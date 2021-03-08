package ru.yakovlev.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import ru.yakovlev.entities.Order;

/**
 * Custom order entity repository.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.5.0
 */
public interface CustomOrderRepository {

    /**
     * Find all orders that can be sent for execution.
     *
     * @param limit maximum number of orders.
     * @return orders that can be sent for execution.
     */
    List<Order> findOrdersForExecution(final int limit);

    /**
     * Find a suitable pair for the given order for their further execution.
     * An explicit lock in the database will be imposed on the found order.
     *
     * @param order order for which you need to find a pair.
     * @param sort orders sort.
     * @return order for execution.
     */
    Optional<Order> findOrderForExecutionByOrderWithSkipLocked(final Order order, @Nullable final Sort sort);

}
