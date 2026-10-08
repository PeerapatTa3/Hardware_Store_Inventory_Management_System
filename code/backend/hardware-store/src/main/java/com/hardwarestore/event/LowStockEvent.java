package com.hardwarestore.event;

/**
 * Event (Subject's notification) published when a product's available stock
 * drops to or below its configured minimum stock level.
 */
public record LowStockEvent(
        Long productId,
        String sku,
        String productName,
        int availableQuantity,
        int minimumStock) {
}
