package com.hardwarestore.service.strategy;

import java.math.BigDecimal;

public class BulkDiscount implements DiscountStrategy {

    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.90");
    private static final int BULK_THRESHOLD = 10;

    @Override
    public BigDecimal apply(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (quantity >= BULK_THRESHOLD) {
            return subtotal.multiply(BULK_DISCOUNT_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return subtotal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
