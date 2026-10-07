package com.hardwarestore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SalesOrderRequest {

    @NotNull(message = "customerId is required")
    @Positive(message = "customerId must be positive")
    private Long customerId;

    @NotEmpty(message = "items must not be empty")
    private List<@NotNull @Valid SalesOrderItemRequest> items;
}
