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

package ru.yakovlev.service;

import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yakovlev.entities.Order;
import ru.yakovlev.repositories.OrderRepository;

/**
 * Orders service.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.4.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class OrdersService {
    private final OrderRepository orderRepository;
    private final PermissionEvaluator permissionEvaluator;

    /**
     * Cancels order execution.
     *
     * @param id order id.
     * @throws EntityNotFoundException if the order with the provided id does not exist
     * @throws AccessDeniedException if the user has no rights to change the order
     */
    @Transactional
    public void cancelOrderExecution(final Long id) throws EntityNotFoundException, AccessDeniedException {
        val order = this.orderRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.permissionEvaluator.hasPermission(authentication, id, Order.class.getName(), BasePermission.WRITE)) {
            order.setCancelled(true);
        } else {
            throw new AccessDeniedException("Access is denied");
        }
    }

}
