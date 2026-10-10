package com.hardwarestore.service;

import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.response.SalesOrderResponse;

import java.util.List;

public interface SalesOrderService {

    SalesOrderResponse create(SalesOrderRequest request);

    List<SalesOrderResponse> findAll();

    SalesOrderResponse findById(Long id);

    SalesOrderResponse update(Long id, SalesOrderRequest request);

    SalesOrderResponse updateStatus(Long id, SalesOrderStatus status);
}
