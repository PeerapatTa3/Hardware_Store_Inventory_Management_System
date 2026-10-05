package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.dto.response.StockMovementResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement stockMovement) {
        if (stockMovement == null) {
            return null;
        }

        return StockMovementResponse.builder()
                .id(stockMovement.getId())
                .productId(stockMovement.getProduct() != null ? stockMovement.getProduct().getId() : null)
                .productName(stockMovement.getProduct() != null ? stockMovement.getProduct().getName() : null)
                .movementType(stockMovement.getMovementType())
                .quantity(stockMovement.getQuantity())
                .referenceNo(stockMovement.getReferenceNo())
                .note(stockMovement.getNote())
                .movementAt(stockMovement.getMovementAt())
                .build();
    }
}
