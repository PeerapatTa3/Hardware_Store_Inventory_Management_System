package com.hardwarestore.exception;

public class InvalidStockMovementException extends RuntimeException {
    public InvalidStockMovementException(String message) {
        super(message);
    }
}