package com.hardwarestore.service.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal apply(BigDecimal unitPrice, int quantity);
}
