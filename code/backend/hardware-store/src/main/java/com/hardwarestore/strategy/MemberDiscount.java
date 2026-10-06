package com.hardwarestore.service.strategy;

import java.math.BigDecimal;

public class MemberDiscount implements DiscountStrategy {

    private static final BigDecimal MEMBER_DISCOUNT_RATE = new BigDecimal("0.90");

    @Override
    public BigDecimal apply(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return subtotal.multiply(MEMBER_DISCOUNT_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
