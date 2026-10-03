package com.hardwarestore.service;

import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;

public interface StockMovementService {
    StockMovementResponse create(StockMovementRequest request);
}
