package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.entity.StockMovementType;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.StockMovementMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.StockMovementRepository;
import com.hardwarestore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        InventoryStock inventory = inventoryStockRepository.findByProductId(product.getId())
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
        if (request.getMovementType() == StockMovementType.IN) {
            if (inventory.getQuantity() > Integer.MAX_VALUE - quantity) {
                throw new IllegalArgumentException(
                        "Inbound quantity exceeds supported stock quantity for product id: " + product.getId());
            }
            inventory.setQuantity(inventory.getQuantity() + quantity);
        } else if (request.getMovementType() == StockMovementType.OUT) {
            int available = inventory.getAvailableQuantity();
            if (available < quantity) {
                throw new IllegalArgumentException("Insufficient available quantity for product id: " + product.getId());
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
        } else if (request.getMovementType() == StockMovementType.ADJUSTMENT) {
            if (quantity < inventory.getReservedQuantity()) {
                throw new IllegalArgumentException(
                        "Adjusted quantity cannot be less than reserved quantity for product id: "
                                + product.getId());
            }
            inventory.setQuantity(quantity);
        }

        inventoryStockRepository.save(inventory);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(quantity);
        movement.setReferenceNo(request.getReferenceNo());
        movement.setNote(request.getNote());
        StockMovement saved = stockMovementRepository.save(movement);

        return stockMovementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findAll() {
        return stockMovementRepository.findAll(Sort.by(Sort.Direction.DESC, "movementAt")).stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByProductId(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return stockMovementRepository.findByProductId(
                        productId, Sort.by(Sort.Direction.DESC, "movementAt")).stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }
}
