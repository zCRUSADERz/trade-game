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

package ru.yakovlev.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yakovlev.service.OrdersForExecution;
import ru.yakovlev.service.OrdersService;

/**
 * Orders controller.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.7.0
 */
@RepositoryRestController
@AllArgsConstructor
public class OrdersController {
    private final OrdersForExecution ordersForExecution;
    private final OrdersService ordersService;

    /**
     * Sends orders for execution that can be executed.
     *
     * @param limit order limit.
     * @return number of orders sent.
     */
    @PostMapping("/orders/send-to-execution")
    public ResponseEntity<Object> sendToExecution(@RequestParam(defaultValue = "100") int limit) {
        final var sentOrders = this.ordersForExecution.sendToExecution(limit);
        return ResponseEntity.ok(sentOrders);
    }

    /**
     * Launches threads that execute orders.
     *
     * @param workers number of threads to runю
     * @return "No Content" http status.
     */
    @PostMapping("/orders/add-workers-for-order-execution")
    public ResponseEntity<Object> addWorkersForOrderExecution(@RequestParam(defaultValue = "1") int workers) {
        for (int i = 0; i < workers; i++) {
            this.ordersForExecution.startExecutionWorker();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Launches the process of batch creation of orders according to the provided parameters.
     *
     * @param params parameters
     * @return "No Content" http status.
     * @throws InterruptedException if the thread was interrupted.
     */
    @PostMapping("/orders/create-orders")
    public ResponseEntity<Object> createOrders(@RequestBody OrdersService.CreateOrdersParams params)
            throws InterruptedException {
        this.ordersService.createOrders(params);
        return ResponseEntity.noContent().build();
    }

}
