package com.hardwarestore.dto.request;

import com.hardwarestore.domain.entity.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "movementType is required")
    private StockMovementType movementType;

    @NotNull(message = "quantity is required")
    @PositiveOrZero(message = "quantity must be zero or greater")
    private Integer quantity;

    private String referenceNo;
    private String note;
}
