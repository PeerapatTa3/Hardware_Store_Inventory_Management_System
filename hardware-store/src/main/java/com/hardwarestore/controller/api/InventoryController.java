package com.hardwarestore.controller.api;

import com.hardwarestore.dto.request.InventoryStockRequest;
import com.hardwarestore.dto.response.InventoryStockResponse;
import com.hardwarestore.service.InventoryStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryStockService inventoryStockService;

    @GetMapping("/products/{productId}")
    public InventoryStockResponse getStockByProductId(@PathVariable Long productId) {
        return inventoryStockService.getStockByProductId(productId);
    }

    @PutMapping("/products/{productId}")
    public InventoryStockResponse adjustStock(@PathVariable Long productId,
                                             @Valid @RequestBody InventoryStockRequest request) {
        return inventoryStockService.adjustStock(productId, request);
    }
}
