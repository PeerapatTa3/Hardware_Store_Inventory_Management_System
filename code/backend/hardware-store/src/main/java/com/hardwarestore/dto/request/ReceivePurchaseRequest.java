package com.hardwarestore.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class ReceivePurchaseRequest {
    
    @NotNull(message = "Items list is required")
    private List<ReceiveItemRequest> items;

    @Data
    public static class ReceiveItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Received quantity is required")
        @PositiveOrZero(message = "Received quantity must be positive or zero")
        private Integer receivedQuantity;
    }
}
