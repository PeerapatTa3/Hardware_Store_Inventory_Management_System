package com.hardwarestore.exception;

public class InvalidSalesOrderStateException extends RuntimeException {
    public InvalidSalesOrderStateException(String message) {
        super(message);
    }
}
