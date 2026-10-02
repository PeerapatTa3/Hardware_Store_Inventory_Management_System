package com.hardwarestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String unit;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer minimumStock;
    private Long categoryId;
    private Long supplierId;
}
