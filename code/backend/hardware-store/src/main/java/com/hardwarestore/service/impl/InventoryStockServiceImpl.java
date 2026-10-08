package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.dto.request.InventoryStockRequest;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.InventoryStockResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.InventoryStockMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.service.InventoryStockService;
import com.hardwarestore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;
    private final ProductRepository productRepository;
    private final InventoryStockMapper inventoryStockMapper;
    private final StockMovementService stockMovementService;

    @Override
    public InventoryStockResponse getStockByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        InventoryStock inventory = inventoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));

        inventory.setProduct(product);
        return inventoryStockMapper.toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryStockResponse adjustStock(Long productId, InventoryStockRequest request) {
        StockMovementRequest movementRequest = new StockMovementRequest();
        movementRequest.setProductId(productId);
        movementRequest.setMovementType(StockMovementType.ADJUSTMENT);
        movementRequest.setQuantity(request.getQuantity());
        movementRequest.setNote(request.getReason());
        stockMovementService.create(movementRequest);

        InventoryStock saved = inventoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product id: " + productId));
        return inventoryStockMapper.toResponse(saved);
    }
}
