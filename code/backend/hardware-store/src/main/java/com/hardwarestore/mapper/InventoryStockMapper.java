package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.dto.response.InventoryStockResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryStockMapper {

    public InventoryStockResponse toResponse(InventoryStock inventoryStock) {
        if (inventoryStock == null) {
            return null;
        }

        return InventoryStockResponse.builder()
                .id(inventoryStock.getId())
                .productId(inventoryStock.getProduct() != null ? inventoryStock.getProduct().getId() : null)
                .productName(inventoryStock.getProduct() != null ? inventoryStock.getProduct().getName() : null)
                .quantity(inventoryStock.getQuantity())
                .reservedQuantity(inventoryStock.getReservedQuantity())
                .availableQuantity(inventoryStock.getAvailableQuantity())
                .build();
    }
}
