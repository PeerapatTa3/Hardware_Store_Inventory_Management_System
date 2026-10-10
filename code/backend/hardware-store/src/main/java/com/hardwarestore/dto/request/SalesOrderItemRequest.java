package com.hardwarestore.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesOrderItemRequest {

    @NotNull(message = "productId is required")
    @Positive(message = "productId must be positive")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;
}
