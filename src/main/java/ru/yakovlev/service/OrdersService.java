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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yakovlev.entities.Order;
import ru.yakovlev.entities.embedded.OrderType;
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

    /**
     * Creates orders.
     *
     * @param params create orders parameters.
     * @throws InterruptedException if the thread was interrupted.
     */
    @SuppressWarnings("BusyWait")
    @Async("simpleAsyncTaskExecutor")
    public void createOrders(@NonNull CreateOrdersParams params) throws InterruptedException {
        log.debug("The process of creating orders has been started, params: {}", params);
        double additionalPrice = params.getMaxPrice() - params.getMinPrice();
        final Supplier<BigDecimal> priceSupplier =
                () -> BigDecimal.valueOf(params.getMinPrice() + additionalPrice * Math.random())
                        .setScale(2, RoundingMode.DOWN);
        final Supplier<OrderType> typeSupplier;
        if (params.isRandomType()) {
            typeSupplier = () -> Math.random() >= 0.5 ? OrderType.SELL : OrderType.BUY;
        } else {
            typeSupplier = params::getOrderType;
        }
        long additionalQuantity = params.getMaxQuantity() - params.getMinQuantity();
        final LongSupplier quantitySupplier =
                () -> (long) (params.getMinQuantity() + additionalQuantity * Math.random());
        final var batchSize = params.getBatchSize();
        for (int i = 0; i < params.getNumberOfBatches(); i++) {
            final List<Order> orders = new ArrayList<>(batchSize);
            for (int y = 0; y < batchSize; y++) {
                orders.add(new Order(typeSupplier.get(), priceSupplier.get(), quantitySupplier.getAsLong()));
            }
            this.orderRepository.saveAll(orders);
            log.trace("?????????????? {} ??????????????", batchSize);
            Thread.sleep(params.getDelayBetweenBatches());
        }
    }

    /**
     * Create orders parameters.
     *
     * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
     * @since 0.4.0
     */
    @Value
    @Jacksonized
    @Builder
    public static class CreateOrdersParams {

        /**
         * Randomly selects the type of order.
         */
        boolean randomType;
        OrderType orderType;
        double minPrice;
        double maxPrice;
        long maxQuantity;
        long minQuantity;
        int numberOfBatches;

        /**
         * Number of orders in a batch.
         */
        int batchSize;

        /**
         * Delay in milliseconds between batch creation.
         */
        int delayBetweenBatches;
    }

}
