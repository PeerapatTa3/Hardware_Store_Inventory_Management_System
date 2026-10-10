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

    private Long customerId;

    private String shippingAddress;
    
    private String paymentMethod;

    @NotEmpty(message = "items must not be empty")
    private List<@NotNull @Valid SalesOrderItemRequest> items;
}
