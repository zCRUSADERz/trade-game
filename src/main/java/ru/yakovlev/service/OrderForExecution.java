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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.yakovlev.entities.Order;
import ru.yakovlev.entities.OrderExecution;
import ru.yakovlev.entities.embedded.OrderType;
import ru.yakovlev.repositories.OrderExecutionRepository;
import ru.yakovlev.repositories.OrderRepository;

/**
 * An order that can be sent for execution.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.5.0
 */
@AllArgsConstructor
@Slf4j
public class OrderForExecution {
    private final Order order;
    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;

    /**
     * Send for execution.
     */
    @Transactional
    public void sendForExecution() {
        if (OrderType.BUY.equals(this.order.getType())) {
            var optOrder = this.orderRepository.findByIdForExecutionWithLock(this.order.getId());
            if (optOrder.isPresent()) {
                var firstOrder = optOrder.get();
                var secondOptOrder = this.orderRepository
                        .findOrderForExecutionByOrderWithSkipLocked(firstOrder, Sort.by("price"));
                secondOptOrder.ifPresent(value -> this.sendForExecution(firstOrder, value));
            }
        } else {
            var firstOptOrder = this.orderRepository
                    .findOrderForExecutionByOrderWithSkipLocked(this.order, Sort.by(Sort.Direction.DESC, "price"));
            if (firstOptOrder.isPresent()) {
                var secondOptOrder = this.orderRepository.findByIdForExecutionWithLock(this.order.getId());
                secondOptOrder.ifPresent(value -> this.sendForExecution(firstOptOrder.get(), value));
            }
        }
    }

    private void sendForExecution(final Order firstOrder, final Order secondOrder) {
        firstOrder.setOnExecution(true);
        secondOrder.setOnExecution(true);
        this.orderExecutionRepository.save(new OrderExecution(firstOrder, secondOrder));
    }
}
