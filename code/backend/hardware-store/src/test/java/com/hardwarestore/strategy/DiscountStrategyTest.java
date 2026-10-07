package com.hardwarestore.service.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountStrategyTest {

    @Test
    void normalDiscountShouldKeepOriginalTotal() {
        DiscountStrategy strategy = new NormalDiscount();

        BigDecimal total = strategy.apply(BigDecimal.valueOf(100), 2);

        assertEquals(new BigDecimal("200.00"), total.setScale(2));
    }

    @Test
    void memberDiscountShouldApplyTenPercentOff() {
        DiscountStrategy strategy = new MemberDiscount();

        BigDecimal total = strategy.apply(BigDecimal.valueOf(100), 2);

        assertEquals(new BigDecimal("180.00"), total.setScale(2));
    }

    @Test
    void bulkDiscountShouldApplyTenPercentOffForLargeOrders() {
        DiscountStrategy strategy = new BulkDiscount();

        BigDecimal total = strategy.apply(BigDecimal.valueOf(100), 10);

        assertEquals(new BigDecimal("900.00"), total.setScale(2));
    }
}
