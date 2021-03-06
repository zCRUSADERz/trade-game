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

import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yakovlev.service.OrdersService;

/**
 * Order controller.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.4.0
 */
@RepositoryRestController
@AllArgsConstructor
public class OrderController {
    private final OrdersService ordersService;

    /**
     * Cancels further execution of the order if it has not been executed yet.
     *
     * @param id order id.
     * @return "No Content" http status.
     */
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<Object> cancel(@PathVariable Long id) {
        try {
            this.ordersService.cancelOrderExecution(id);
        } catch (EntityNotFoundException ex) {
            throw new ResourceNotFoundException();
        }
        return ResponseEntity.noContent().build();
    }
}
