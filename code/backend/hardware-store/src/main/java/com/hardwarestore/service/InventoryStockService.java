package com.hardwarestore.service;

import com.hardwarestore.dto.request.InventoryStockRequest;
import com.hardwarestore.dto.response.InventoryStockResponse;

public interface InventoryStockService {

    InventoryStockResponse getStockByProductId(Long productId);

    InventoryStockResponse adjustStock(Long productId, InventoryStockRequest request);
}
