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
import com.hardwarestore.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryStockServiceImplTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryStockMapper inventoryStockMapper;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private InventoryStockServiceImpl inventoryStockService;

    @Test
    void getStockByProductIdShouldReturnInventory() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Hammer");

        InventoryStock inventory = new InventoryStock();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setQuantity(25);
        inventory.setReservedQuantity(3);

        InventoryStockResponse response = InventoryStockResponse.builder()
                .id(10L)
                .productId(5L)
                .productName("Hammer")
                .quantity(25)
                .reservedQuantity(3)
                .availableQuantity(22)
                .build();

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(5L)).thenReturn(Optional.of(inventory));
        when(inventoryStockMapper.toResponse(inventory)).thenReturn(response);

        InventoryStockResponse result = inventoryStockService.getStockByProductId(5L);

        assertNotNull(result);
        assertEquals(25, result.getQuantity());
        assertEquals(22, result.getAvailableQuantity());
    }

    @Test
    void adjustStockShouldUpdateQuantity() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Hammer");

        InventoryStock inventory = new InventoryStock();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(2);

        InventoryStockRequest request = new InventoryStockRequest();
        request.setQuantity(15);
        request.setReason("manual adjustment");

        InventoryStockResponse response = InventoryStockResponse.builder()
                .id(10L)
                .productId(5L)
                .productName("Hammer")
                .quantity(15)
                .reservedQuantity(2)
                .availableQuantity(13)
                .build();

        when(inventoryStockRepository.findByProductId(5L)).thenReturn(Optional.of(inventory));
        when(inventoryStockMapper.toResponse(inventory)).thenReturn(response);

        InventoryStockResponse result = inventoryStockService.adjustStock(5L, request);

        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        ArgumentCaptor<StockMovementRequest> movementRequestCaptor =
                ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(stockMovementService).create(movementRequestCaptor.capture());
        assertEquals(5L, movementRequestCaptor.getValue().getProductId());
        assertEquals(StockMovementType.ADJUSTMENT, movementRequestCaptor.getValue().getMovementType());
        assertEquals(15, movementRequestCaptor.getValue().getQuantity());
        assertEquals("manual adjustment", movementRequestCaptor.getValue().getNote());
        verify(inventoryStockRepository, never()).save(any(InventoryStock.class));
    }

    @Test
    void getStockByProductIdShouldThrowIfProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryStockService.getStockByProductId(99L));
    }

    @Test
    void getStockByProductIdShouldThrowIfInventoryMissing() {
        Product product = new Product();
        product.setId(99L);
        when(productRepository.findById(99L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryStockService.getStockByProductId(99L));
    }
}
