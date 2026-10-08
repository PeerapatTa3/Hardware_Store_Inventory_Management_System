package com.hardwarestore.dto.response;

import com.hardwarestore.domain.enums.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private SalesOrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<SalesOrderItemResponse> items;
}
