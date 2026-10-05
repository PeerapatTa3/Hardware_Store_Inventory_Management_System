package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.dto.request.InventoryStockRequest;
import com.hardwarestore.dto.response.InventoryStockResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.InventoryStockMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(5L)).thenReturn(Optional.of(inventory));
        when(inventoryStockRepository.save(inventory)).thenReturn(inventory);
        when(inventoryStockMapper.toResponse(inventory)).thenReturn(response);

        InventoryStockResponse result = inventoryStockService.adjustStock(5L, request);

        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        verify(inventoryStockRepository).save(inventory);
    }

    @Test
    void getStockByProductIdShouldThrowIfProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryStockService.getStockByProductId(99L));
    }
}
