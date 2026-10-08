package com.hardwarestore.dto.request;

import com.hardwarestore.domain.entity.SalesOrder.SalesOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesOrderStatusRequest {

    @NotNull(message = "status is required")
    private SalesOrderStatus status;
}
