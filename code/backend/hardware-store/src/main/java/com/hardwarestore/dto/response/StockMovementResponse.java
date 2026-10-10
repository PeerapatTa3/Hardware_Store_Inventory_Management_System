package com.hardwarestore.dto.response;

import com.hardwarestore.domain.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private StockMovementType movementType;
    private Integer quantity;
    private String referenceNo;
    private String note;
    private LocalDateTime movementAt;
}
