package ru.yakovlev.model;

import java.math.BigDecimal;
import lombok.Value;
import ru.yakovlev.entities.embedded.OrderType;

/**
 * Price level information.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.10.0
 */
@Value
public class PriceLevelInfo {
    BigDecimal price;
    OrderType type;
    Long ordersCount;
    Long overallQuantity;
}
