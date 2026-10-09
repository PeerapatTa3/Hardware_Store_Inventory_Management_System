package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.event.LowStockEvent;
import com.hardwarestore.mapper.StockMovementMapper;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies the Observer pattern: low stock publishes a {@link LowStockEvent}. */
@ExtendWith(MockitoExtension.class)
class StockMovementLowStockEventTest {

    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryStockRepository inventoryStockRepository;
    @Mock
    private StockMovementMapper stockMovementMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StockMovementServiceImpl service;

    private Product product;
    private InventoryStock stock;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(10L);
        product.setSku("SKU-001");
        product.setName("Hammer");
        product.setMinimumStock(5);

        stock = new InventoryStock();
        stock.setProduct(product);
        stock.setQuantity(8);
        stock.setReservedQuantity(0);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));
    }

    private StockMovementRequest outRequest(int quantity) {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(10L);
        request.setMovementType(StockMovementType.OUT);
        request.setQuantity(quantity);
        return request;
    }

    @Test
    void create_shouldPublishLowStockEventWhenAvailableDropsToMinimum() {
        service.create(outRequest(3)); // 8 - 3 = 5 <= minimum 5

        ArgumentCaptor<LowStockEvent> captor = ArgumentCaptor.forClass(LowStockEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(10L, captor.getValue().productId());
        assertEquals(5, captor.getValue().availableQuantity());
        assertEquals(5, captor.getValue().minimumStock());
    }

    @Test
    void create_shouldNotPublishEventWhenStockIsAboveMinimum() {
        service.create(outRequest(2)); // 8 - 2 = 6 > minimum 5

        verify(eventPublisher, never()).publishEvent(any(LowStockEvent.class));
    }
}
