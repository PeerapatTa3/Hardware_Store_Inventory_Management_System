package com.hardwarestore.service.strategy;

import java.math.BigDecimal;

public class NormalDiscount implements DiscountStrategy {

    @Override
    public BigDecimal apply(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
