/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Yakovlev Alexander
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.yakovlev.repositories;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import ru.yakovlev.entities.Order;
import ru.yakovlev.model.PriceLevelInfo;

/**
 * Order entity repository.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@RepositoryRestResource
public interface OrderRepository extends JpaRepository<Order, Long>, CustomOrderRepository {
    String FIND_ALL_QUERY = "SELECT o FROM Order AS o WHERE EXISTS (" +
            "    SELECT 1 FROM AclSid As s " +
            "       JOIN AclEntry AS e ON e.securityId = s " +
            "       JOIN AclObjectIdentity AS i_parent ON i_parent = e.acl " +
            "       JOIN AclObjectIdentity AS i ON i = i_parent.parent OR i = i_parent " +
            "       JOIN AclClass AS c ON i.objectClass = c " +
            "    WHERE (s.principal = true AND s.securityId = :#{principal.username} " +
            "           OR s.principal = false AND s.securityId IN :#{@securityService.userAuthorities()}) " +
            "       AND (MOD(e.mask, 2) = 1 AND e.granting = true OR MOD(e.mask, 2) = 0 AND e.granting = false) " +
            "       AND c.clazz = 'ru.yakovlev.entities.Order' " +
            "       AND i.objectIdIdentity = CONCAT('', o.id))";

    @Override
    @Query(FIND_ALL_QUERY)
    List<Order> findAll();

    @Override
    @Query(FIND_ALL_QUERY)
    List<Order> findAll(Sort sort);

    @Override
    @Query(FIND_ALL_QUERY)
    @RestResource
    Page<Order> findAll(Pageable pageable);

    @Override
    @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject.get(), 'READ') : true")
    @RestResource
    Optional<Order> findById(Long id);

    @RestResource
    Order save(Order order);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order AS o " +
            "WHERE o.onExecution = false AND o.cancelled = false AND o.fullyExecuted = false AND o.id = :id")
    Optional<Order> findByIdForExecutionWithLock(Long id);

    @Query("SELECT new ru.yakovlev.model.PriceLevelInfo(" +
            "   o.price, o.type, COUNT(o.id), SUM(o.quantityLeftover)) " +
            "FROM Order AS o " +
            "WHERE o.fullyExecuted = false AND o.cancelled = false " +
            "GROUP BY o.price, o.type " +
            "ORDER BY o.price, o.type")
    List<PriceLevelInfo> depthOfMarket();

}
