package com.hardwarestore.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal apply(BigDecimal unitPrice, int quantity);
}
