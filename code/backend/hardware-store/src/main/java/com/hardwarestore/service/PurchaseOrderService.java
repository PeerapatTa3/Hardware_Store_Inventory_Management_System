package com.hardwarestore.service;

import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse create(PurchaseOrderRequest request);

    List<PurchaseOrderResponse> findAll();

    PurchaseOrderResponse findById(Long id);

    PurchaseOrderResponse update(Long id, PurchaseOrderRequest request);

    PurchaseOrderResponse receive(Long id);
}
