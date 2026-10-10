package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.domain.enums.StockMovementStatus;
import com.hardwarestore.exception.InvalidPurchaseStateException;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.event.LowStockEvent;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.StockMovementMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.StockMovementRepository;
import com.hardwarestore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(quantity);
        movement.setReferenceNo(request.getReferenceNo());
        movement.setNote(request.getNote());

        if (request.getMovementType() == StockMovementType.ADJUSTMENT) {
            movement.setStatus(StockMovementStatus.PENDING);
        } else {
            request.getMovementType().process(inventory, quantity);
            inventoryStockRepository.save(inventory);
            movement.setStatus(StockMovementStatus.APPROVED);
        }

        StockMovement saved = stockMovementRepository.save(movement);
        if (movement.getStatus() == StockMovementStatus.APPROVED) {
            publishLowStockEventIfNeeded(product, inventory);
        }
        return stockMovementMapper.toResponse(saved);
    }

    private void publishLowStockEventIfNeeded(Product product, InventoryStock inventory) {
        Integer minimumStock = product.getMinimumStock();
        if (minimumStock == null) {
            return;
        }
        int available = inventory.getAvailableQuantity();
        if (available <= minimumStock) {
            eventPublisher.publishEvent(new LowStockEvent(
                    product.getId(), product.getSku(), product.getName(), available, minimumStock));
        }
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

    @Override
    @Transactional
    public StockMovementResponse approve(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
        if (movement.getStatus() != StockMovementStatus.PENDING) {
            throw new InvalidPurchaseStateException("Only PENDING stock movements can be approved.");
        }
        
        InventoryStock inventory = inventoryStockRepository.findByProductId(movement.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
                
        movement.getMovementType().process(inventory, movement.getQuantity());
        inventoryStockRepository.save(inventory);
        
        movement.setStatus(StockMovementStatus.APPROVED);
        StockMovement saved = stockMovementRepository.save(movement);
        publishLowStockEventIfNeeded(movement.getProduct(), inventory);
        return stockMovementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StockMovementResponse reject(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
        if (movement.getStatus() != StockMovementStatus.PENDING) {
            throw new InvalidPurchaseStateException("Only PENDING stock movements can be rejected.");
        }
        movement.setStatus(StockMovementStatus.REJECTED);
        return stockMovementMapper.toResponse(stockMovementRepository.save(movement));
    }
}
