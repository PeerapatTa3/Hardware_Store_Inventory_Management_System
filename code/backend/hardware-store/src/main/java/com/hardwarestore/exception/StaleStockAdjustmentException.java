package com.hardwarestore.exception;

public class StaleStockAdjustmentException extends RuntimeException {
    public StaleStockAdjustmentException(String message) {
        super(message);
    }
}
