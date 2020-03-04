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

package ru.yakovlev.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Order execution entity.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_execution_seq_generator")
    @SequenceGenerator(name = "order_execution_seq_generator", sequenceName = "order_execution_id_seq")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(updatable = false)
    private Order fromOrder;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(updatable = false)
    private Order toOrder;

    @Version
    private Integer version;

    public OrderExecution(Order fromOrder, Order toOrder) {
        this(null, fromOrder, toOrder, null);
    }
}
