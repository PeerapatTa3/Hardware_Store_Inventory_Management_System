package com.hardwarestore.service;

import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;

import java.util.List;

public interface StockMovementService {
    StockMovementResponse create(StockMovementRequest request);

    List<StockMovementResponse> findAll();

    List<StockMovementResponse> findByProductId(Long productId);
}
