package com.hardwarestore.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryStockRequest {

    @NotNull(message = "quantity is required")
    @PositiveOrZero(message = "quantity must be zero or greater")
    private Integer quantity;

    private String reason;
}
