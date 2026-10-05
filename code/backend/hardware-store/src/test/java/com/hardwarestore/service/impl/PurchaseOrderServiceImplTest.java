package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.entity.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseItemRequest;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import com.hardwarestore.exception.InvalidPurchaseStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.PurchaseOrderMapper;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.PurchaseOrderRepository;
import com.hardwarestore.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void createShouldResolveReferencesGenerateNumberAndSaveOrder() {
        Supplier supplier = new Supplier();
        supplier.setId(7L);
        Product hammer = product(1L, "Hammer");
        Product wrench = product(2L, "Wrench");
        PurchaseOrderRequest request = request(7L, item(1L), item(2L), item(1L));
        PurchaseOrder mappedOrder = new PurchaseOrder();
        mappedOrder.setStatus(PurchaseOrderStatus.PENDING);
        PurchaseOrder savedOrder = new PurchaseOrder();
        savedOrder.setId(15L);
        PurchaseOrderResponse response = PurchaseOrderResponse.builder().id(15L).build();

        when(supplierRepository.findById(7L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(hammer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(wrench));
        when(purchaseOrderMapper.toEntity(any(PurchaseOrderRequest.class), any(String.class),
                any(Supplier.class), any(Map.class))).thenReturn(mappedOrder);
        when(purchaseOrderRepository.save(mappedOrder)).thenReturn(savedOrder);
        when(purchaseOrderMapper.toResponse(savedOrder)).thenReturn(response);

        PurchaseOrderResponse result = purchaseOrderService.create(request);

        assertSame(response, result);
        verify(purchaseOrderMapper).toEntity(eq(request), argThat(number -> number.startsWith("PO-")),
                same(supplier), eq(Map.of(1L, hammer, 2L, wrench)));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(2L);
        verify(purchaseOrderRepository).save(mappedOrder);
    }

    @Test
    void createShouldFailWhenSupplierDoesNotExist() {
        PurchaseOrderRequest request = request(7L, item(1L));
        when(supplierRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.create(request));

        verifyNoInteractions(productRepository, purchaseOrderMapper, purchaseOrderRepository);
    }

    @Test
    void createShouldFailWhenProductDoesNotExist() {
        PurchaseOrderRequest request = request(7L, item(99L));
        when(supplierRepository.findById(7L)).thenReturn(Optional.of(new Supplier()));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.create(request));

        assertTrue(exception.getMessage().contains("Product not found"));
        verifyNoInteractions(purchaseOrderMapper, purchaseOrderRepository);
    }

    @Test
    void findAllShouldMapOrdersInCreatedDateDescendingOrder() {
        PurchaseOrder order = new PurchaseOrder();
        PurchaseOrderResponse response = PurchaseOrderResponse.builder().id(5L).build();
        when(purchaseOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")))
                .thenReturn(List.of(order));
        when(purchaseOrderMapper.toResponse(order)).thenReturn(response);

        List<PurchaseOrderResponse> result = purchaseOrderService.findAll();

        assertEquals(List.of(response), result);
        verify(purchaseOrderRepository).findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void findByIdShouldMapPurchaseOrder() {
        PurchaseOrder order = new PurchaseOrder();
        PurchaseOrderResponse response = PurchaseOrderResponse.builder().id(12L).build();
        when(purchaseOrderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(purchaseOrderMapper.toResponse(order)).thenReturn(response);

        assertSame(response, purchaseOrderService.findById(12L));
    }

    @Test
    void findByIdShouldThrowWhenPurchaseDoesNotExist() {
        when(purchaseOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.findById(404L));
    }

    @Test
    void updateShouldReplacePendingOrderSupplierItemsAndTotal() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(12L);
        order.setPurchaseNumber("PO-KEEP");
        order.setStatus(PurchaseOrderStatus.PENDING);
        Supplier supplier = new Supplier();
        supplier.setId(7L);
        Product hammer = product(1L, "Hammer");
        PurchaseOrderRequest request = request(7L, item(1L));
        PurchaseOrderResponse response = PurchaseOrderResponse.builder().id(12L).build();

        when(purchaseOrderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(7L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(hammer));
        when(purchaseOrderRepository.save(order)).thenReturn(order);
        when(purchaseOrderMapper.toResponse(order)).thenReturn(response);

        PurchaseOrderResponse result = purchaseOrderService.update(12L, request);

        assertSame(response, result);
        verify(purchaseOrderMapper).updatePendingOrder(order, request, supplier, Map.of(1L, hammer));
        verify(purchaseOrderRepository).save(order);
        verify(purchaseOrderMapper, never()).toEntity(any(), any(), any(), any());
    }

    @Test
    void updateShouldRejectNonPendingPurchaseWithoutChangingIt() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(12L);
        order.setStatus(PurchaseOrderStatus.COMPLETED);
        PurchaseOrderRequest request = request(7L, item(1L));
        when(purchaseOrderRepository.findById(12L)).thenReturn(Optional.of(order));

        InvalidPurchaseStateException exception = assertThrows(InvalidPurchaseStateException.class,
                () -> purchaseOrderService.update(12L, request));

        assertTrue(exception.getMessage().contains("Only pending purchases can be edited"));
        verifyNoInteractions(supplierRepository, productRepository, purchaseOrderMapper);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void updateShouldFailWhenPurchaseDoesNotExist() {
        when(purchaseOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.update(404L, request(7L, item(1L))));

        verifyNoInteractions(supplierRepository, productRepository, purchaseOrderMapper);
    }

    private PurchaseOrderRequest request(Long supplierId, PurchaseItemRequest... items) {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(supplierId);
        request.setItems(List.of(items));
        return request;
    }

    private PurchaseItemRequest item(Long productId) {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setProductId(productId);
        item.setQuantity(2);
        item.setUnitCost(new BigDecimal("8.50"));
        return item;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }
}
