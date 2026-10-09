package com.hardwarestore.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NormalDiscount implements DiscountStrategy {

    @Override
    public BigDecimal apply(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}

