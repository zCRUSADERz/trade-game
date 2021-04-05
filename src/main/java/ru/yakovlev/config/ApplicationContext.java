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

package ru.yakovlev.config;

import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.yakovlev.ApplicationEventListener;
import ru.yakovlev.entities.Order;
import ru.yakovlev.repositories.OrderExecutionRepository;
import ru.yakovlev.repositories.OrderRepository;
import ru.yakovlev.service.OrderForExecution;
import ru.yakovlev.service.OrdersForExecution;

/**
 * Application context configuration.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.5.0
 */
@Configuration
@AllArgsConstructor
public class ApplicationContext {

    @Bean
    ApplicationEventListener eventListener(final OrdersForExecution ordersForExecution,
                                           final OrderExecutionProperties orderExecutionProperties) {
        return new ApplicationEventListener(ordersForExecution, orderExecutionProperties.getWorkers());
    }

    @Bean
    @Scope("prototype")
    OrderForExecution orderForExecution(final Order order, final OrderRepository orderRepository,
                                        final OrderExecutionRepository orderExecutionRepository) {
        return new OrderForExecution(order, orderRepository, orderExecutionRepository);
    }

    @Bean
    OrdersForExecution orderExecutions(final OrderRepository orderRepository,
                                       final OrderExecutionRepository orderExecutionRepository) {
        return new OrdersForExecution(new LinkedBlockingQueue<>(),
                order -> this.orderForExecution(order, orderRepository, orderExecutionRepository), orderRepository);
    }

}
