package ru.yakovlev.repositories;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.hibernate.LockOptions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.lang.Nullable;
import ru.yakovlev.entities.Order;
import ru.yakovlev.entities.embedded.OrderType;

/**
 * Custom order entity repository implementation.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.5.0
 */
@AllArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {
    private final EntityManager entityManager;

    @Override
    public List<Order> findOrdersForExecution(final int limit) {
        return this.entityManager.createQuery(
                "SELECT o FROM Order AS o "
                        + "WHERE o.onExecution = false AND o.cancelled = false AND o.fullyExecuted = false "
                        + "   AND ((o.type = 'BUY' AND o.price >= ("
                        + "           SELECT min(o.price) FROM Order AS o "
                        + "           WHERE o.type = 'SELL' AND o.onExecution = false AND o.cancelled = false "
                        + "               AND o.fullyExecuted = false)) "
                        + "       OR (o.type = 'SELL' AND o.price <= ("
                        + "           SELECT max(o.price) FROM Order AS o "
                        + "           WHERE o.type = 'BUY' AND o.onExecution = false AND o.cancelled = false "
                        + "               AND o.fullyExecuted = false)))",
                Order.class
        ).setMaxResults(limit).getResultList();
    }

    @Override
    public Optional<Order> findOrderForExecutionByOrderWithSkipLocked(final Order order, @Nullable final Sort sort) {
        try {
            final var builder = this.entityManager.getCriteriaBuilder();
            final var query = builder.createQuery(Order.class);
            final var root = query.from(Order.class);
            final var onExecutionPredicate = builder.isFalse(root.get("onExecution"));
            final var cancelledPredicate = builder.isFalse(root.get("cancelled"));
            final var fullyExecutedPredicate = builder.isFalse(root.get("fullyExecuted"));
            final Predicate typePredicate;
            final Predicate pricePredicate;
            switch (order.getType()) {
                case BUY:
                    typePredicate = builder.equal(root.get("type"), OrderType.SELL);
                    pricePredicate = builder.lessThanOrEqualTo(root.get("price"), order.getPrice());
                    break;
                case SELL:
                    typePredicate = builder.equal(root.get("type"), OrderType.BUY);
                    pricePredicate = builder.greaterThanOrEqualTo(root.get("price"), order.getPrice());
                    break;
                default:
                    throw new IllegalStateException("Unknown order type " + order.getType());
            }
            query.where(
                    onExecutionPredicate, cancelledPredicate, fullyExecutedPredicate, typePredicate, pricePredicate);
            final var byId = builder.asc(root.get("id"));
            if (Objects.nonNull(sort)) {
                final var listOfOrders = QueryUtils.toOrders(sort, root, builder);
                listOfOrders.add(byId);
                query.orderBy(listOfOrders);
            } else {
                query.orderBy(byId);
            }
            return Optional.of(this.entityManager.createQuery(query)
                    .setMaxResults(1)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .setHint("javax.persistence.lock.timeout", LockOptions.SKIP_LOCKED)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
