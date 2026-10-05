package com.hardwarestore.exception;

public class DuplicateSkuException extends RuntimeException {
    public DuplicateSkuException(String sku) {
        super("Product with sku " + sku + " already exists");
    }
}