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

package ru.yakovlev.entities.listeners;

import java.util.Objects;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.yakovlev.entities.Order;
import ru.yakovlev.service.OrdersForExecution;
import ru.yakovlev.service.security.SecurityService;

/**
 * Order event listener.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@NoArgsConstructor
public class OrderListener {
    private Listener listener;

    @PostPersist
    public void afterPersist(final Order order) {
        this.init();
        this.listener.afterPersist(order);
    }

    @PostUpdate
    public void afterUpdate(final Order order) {
        this.init();
        this.listener.afterUpdate(order);
    }

    private void init() {
        if (Objects.isNull(this.listener)) {
            this.listener = new Listener();
        }
    }

    /**
     * Order event listener.
     *
     * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
     * @since 0.3.0
     */
    @Configurable
    public static class Listener {
        @Autowired
        private OrdersForExecution ordersForExecution;
        @Autowired
        private SecurityService securityService;

        public void afterPersist(Order order) {
            this.securityService.createAcl(order);
            this.notifyOrdersForExecution(order);
        }

        public void afterUpdate(Order order) {
            this.notifyOrdersForExecution(order);
        }

        private void notifyOrdersForExecution(Order order) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @SneakyThrows
                @Override
                public void afterCommit() {
                    ordersForExecution.notify(order);
                }
            });
        }

    }
}
