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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.yakovlev.entities.embedded.OrderType;

/**
 * Order entity.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.3.0
 */
@Entity
@Table(name = "\"order\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_execution_seq_generator")
    @SequenceGenerator(name = "order_execution_seq_generator", sequenceName = "order_execution_id_seq")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrderType type;

    @Digits(integer = 16, fraction = 2)
    @DecimalMin(value = "0", inclusive = false)
    @NotNull
    @Column(nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Min(1)
    @NotNull
    @Column(nullable = false, updatable = false)
    private long quantity;

    @NotNull
    @Setter
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean onExecution;

    @NotNull
    @Setter
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean cancelled;

    @NotNull
    @Setter
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean fullyExecuted;

    @NotNull
    @Setter
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long quantityLeftover = quantity;

    @Version
    private Integer version;

    @JsonCreator
    public Order(@NotNull final OrderType type, @DecimalMin("0") @NotNull final BigDecimal price,
                 @Min(0) @NotNull final long quantity) {
        this(null, type, price, quantity, false, false, false, quantity, null);
    }
}
