package com.hardwarestore.validation;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.repository.InventoryStockRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Checks that available stock covers the requested quantities. On edits the
 * quantities previously held by the same order are added back before comparing.
 */
public class StockAvailableHandler extends OrderValidationHandler {

    private final InventoryStockRepository inventoryStockRepository;

    public StockAvailableHandler(InventoryStockRepository inventoryStockRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
    }

    @Override
    protected void validate(OrderValidationContext context) {
        Map<Long, Integer> previousQuantitiesByProduct = context.getPreviousQuantitiesByProduct();
        Map<Long, Integer> requestedQuantitiesByProduct = new HashMap<>();
        for (SalesOrderItemRequest item : context.getRequest().getItems()) {
            requestedQuantitiesByProduct.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        for (Long productId : Stream.concat(
                        previousQuantitiesByProduct.keySet().stream(),
                        requestedQuantitiesByProduct.keySet().stream())
                .distinct()
                .toList()) {
            InventoryStock inventory = inventoryStockRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product id: " + productId));

            int previousQuantity = previousQuantitiesByProduct.getOrDefault(productId, 0);
            int requestedQuantity = requestedQuantitiesByProduct.getOrDefault(productId, 0);
            int availableAfterAdjustment = inventory.getAvailableQuantity() + previousQuantity - requestedQuantity;

            if (availableAfterAdjustment < 0) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product id: " + productId
                                + ". Available: " + inventory.getAvailableQuantity()
                                + ", previous quantity: " + previousQuantity
                                + ", requested: " + requestedQuantity);
            }
        }
    }
}
