package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.entity.StockMovementType;
import com.hardwarestore.dto.request.InventoryStockRequest;
import com.hardwarestore.dto.response.InventoryStockResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.exception.InvalidStockMovementException;
import com.hardwarestore.mapper.InventoryStockMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.StockMovementRepository;
import com.hardwarestore.service.InventoryStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;
    private final ProductRepository productRepository;
    private final InventoryStockMapper inventoryStockMapper;
    private final StockMovementRepository stockMovementRepository;

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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        InventoryStock inventory = inventoryStockRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setProduct(product);
                    newStock.setQuantity(0);
                    newStock.setReservedQuantity(0);
                    return newStock;
                });

        inventory.setProduct(product);
        if (request.getQuantity() < inventory.getReservedQuantity()) {
            throw new InvalidStockMovementException("Adjusted quantity cannot be less than reserved quantity");
        }
        int quantityChange = request.getQuantity() - inventory.getQuantity();
        inventory.setQuantity(request.getQuantity());

        InventoryStock saved = inventoryStockRepository.save(inventory);
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(StockMovementType.ADJUSTMENT);
        movement.setQuantity(quantityChange);
        movement.setNote(request.getReason());
        stockMovementRepository.save(movement);
        return inventoryStockMapper.toResponse(saved);
    }
}
