package com.hardwarestore.dto.request;

import com.hardwarestore.domain.enums.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive(message = "quantity must be positive")
    private Integer quantity;

    private String referenceNo;
    private String note;
}
