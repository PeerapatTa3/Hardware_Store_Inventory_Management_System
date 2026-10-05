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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(10L);
        product.setSku("SKU-001");
        product.setName("Hammer");
    }

    @Test
    void create_shouldAddStockForInboundMovement() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(10L);
        request.setMovementType(StockMovementType.IN);
        request.setQuantity(5);
        request.setReferenceNo("PO-1001");
        request.setNote("Receive stock");

        InventoryStock stock = new InventoryStock();
        stock.setId(1L);
        stock.setProduct(product);
        stock.setQuantity(8);
        stock.setReservedQuantity(1);

        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(99L);
        savedMovement.setProduct(product);
        savedMovement.setMovementType(StockMovementType.IN);
        savedMovement.setQuantity(5);
        savedMovement.setReferenceNo("PO-1001");
        savedMovement.setNote("Receive stock");

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(stockMovementMapper.toResponse(savedMovement)).thenReturn(StockMovementResponse.builder()
                .id(99L)
                .productId(10L)
                .productName("Hammer")
                .movementType(StockMovementType.IN)
                .quantity(5)
                .referenceNo("PO-1001")
                .note("Receive stock")
                .build());

        StockMovementResponse result = stockMovementService.create(request);

        assertNotNull(result);
        assertEquals(13, stock.getQuantity());
        assertEquals(StockMovementType.IN, result.getMovementType());
        assertEquals("PO-1001", result.getReferenceNo());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void create_shouldReduceStockForOutboundMovement() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(10L);
        request.setMovementType(StockMovementType.OUT);
        request.setQuantity(3);
        request.setReferenceNo("SO-2001");
        request.setNote("Sale");

        InventoryStock stock = new InventoryStock();
        stock.setId(1L);
        stock.setProduct(product);
        stock.setQuantity(12);
        stock.setReservedQuantity(2);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(new StockMovement());
        when(stockMovementMapper.toResponse(any(StockMovement.class))).thenReturn(StockMovementResponse.builder()
                .productId(10L)
                .productName("Hammer")
                .movementType(StockMovementType.OUT)
                .quantity(3)
                .referenceNo("SO-2001")
                .note("Sale")
                .build());

        StockMovementResponse result = stockMovementService.create(request);

        assertNotNull(result);
        assertEquals(9, stock.getQuantity());
        assertEquals(StockMovementType.OUT, result.getMovementType());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void create_shouldThrowWhenProductDoesNotExist() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(99L);
        request.setMovementType(StockMovementType.IN);
        request.setQuantity(2);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockMovementService.create(request));
    }
}
