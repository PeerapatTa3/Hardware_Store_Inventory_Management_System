package com.hardwarestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "sku must not be blank")
    @Size(max = 100, message = "sku must not exceed 100 characters")
    private String sku;

    @NotBlank(message = "name must not be blank")
    @Size(max = 150, message = "name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "unit must not be blank")
    @Size(max = 50, message = "unit must not exceed 50 characters")
    private String unit;

    @NotNull(message = "price is required")
    @Positive(message = "price must be positive")
    private BigDecimal price;

    @NotNull(message = "costPrice is required")
    @Positive(message = "costPrice must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "minimumStock is required")
    @Positive(message = "minimumStock must be positive")
    private Integer minimumStock;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "supplierId is required")
    private Long supplierId;
}
