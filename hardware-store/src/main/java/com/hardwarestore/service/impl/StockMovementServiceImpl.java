package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.entity.StockMovementType;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.exception.InsufficientStockException;
import com.hardwarestore.exception.InvalidStockMovementException;
import com.hardwarestore.mapper.StockMovementMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.StockMovementRepository;
import com.hardwarestore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final StockMovementMapper stockMovementMapper;

    @Override
    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        InventoryStock inventory = inventoryStockRepository.findByProductIdForUpdate(product.getId())
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setProduct(product);
                    newStock.setQuantity(0);
                    newStock.setReservedQuantity(0);
                    return newStock;
                });

        if (inventory.getProduct() == null) {
            inventory.setProduct(product);
        }

        int quantity = request.getQuantity();
        int movementQuantity = quantity;
        if (request.getMovementType() == StockMovementType.IN) {
            requirePositiveQuantity(quantity);
            inventory.setQuantity(inventory.getQuantity() + quantity);
        } else if (request.getMovementType() == StockMovementType.OUT) {
            requirePositiveQuantity(quantity);
            int available = inventory.getAvailableQuantity();
            if (available < quantity) {
                throw new InsufficientStockException("Insufficient available quantity for product id: " + product.getId());
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
        } else if (request.getMovementType() == StockMovementType.ADJUSTMENT) {
            if (quantity < inventory.getReservedQuantity()) {
                throw new InvalidStockMovementException("Adjusted quantity cannot be less than reserved quantity");
            }
            movementQuantity = quantity - inventory.getQuantity();
            inventory.setQuantity(quantity);
        } else {
            throw new InvalidStockMovementException("Unsupported stock movement type");
        }

        inventoryStockRepository.save(inventory);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(movementQuantity);
        movement.setReferenceNo(request.getReferenceNo());
        movement.setNote(request.getNote());
        StockMovement saved = stockMovementRepository.save(movement);

        return stockMovementMapper.toResponse(saved);
    }

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockMovementException("IN and OUT movement quantities must be greater than zero");
        }
    }
}
