package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.SalesOrderResponse;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.exception.InvalidSalesOrderStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.SalesOrderMapper;
import com.hardwarestore.repository.CustomerRepository;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.SalesOrderRepository;
import com.hardwarestore.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hardwarestore.domain.enums.SalesOrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    @Test
    void createShouldPersistOrderAndRecordStockOut() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Hammer");

        SalesOrder order = new SalesOrder();
        order.setId(99L);
        order.setCustomer(customer);
        order.setStatus(PENDING);
        order.setItems(List.of(createItem(order, product, 2, new BigDecimal("150.00"))));

        SalesOrderResponse response = SalesOrderResponse.builder()
                .id(99L)
                .customerId(1L)
                .status(PENDING)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(createInventoryStock(product, 10)));
        when(salesOrderMapper.toEntity(any(SalesOrderRequest.class), eq(customer), anyMap())).thenReturn(order);
        when(salesOrderRepository.save(order)).thenReturn(order);
        when(stockMovementService.create(any(StockMovementRequest.class))).thenReturn(new StockMovementResponse());
        when(salesOrderMapper.toResponse(order)).thenReturn(response);

        SalesOrderResponse result = salesOrderService.create(request);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        verify(salesOrderRepository).save(order);
        verify(stockMovementService).create(any(StockMovementRequest.class));
    }

    @Test
    void createShouldThrowWhenCustomerDoesNotExist() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(99L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> salesOrderService.create(request));
        verifyNoInteractions(productRepository);
    }

    @Test
    void createShouldThrowWhenProductDoesNotExist() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> salesOrderService.create(request));
    }

    @Test
    void createShouldThrowWhenInventoryStockIsInsufficient() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(3);
        request.setItems(List.of(itemRequest));

        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(10L);

        SalesOrder order = new SalesOrder();
        order.setStatus(PENDING);
        order.setItems(List.of(createItem(order, product, 3, new BigDecimal("40.00"))));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(createInventoryStock(product, 2)));

        assertThrows(IllegalArgumentException.class, () -> salesOrderService.create(request));
    }

    @Test
    void findByIdShouldReturnSalesOrder() {
        SalesOrder order = new SalesOrder();
        order.setId(12L);
        order.setStatus(PENDING);

        SalesOrderResponse response = SalesOrderResponse.builder().id(12L).status(PENDING).build();

        when(salesOrderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(salesOrderMapper.toResponse(order)).thenReturn(response);

        assertEquals(response, salesOrderService.findById(12L));
    }

    @Test
    void findAllShouldReturnSalesOrders() {
        SalesOrder order = new SalesOrder();
        order.setId(3L);
        order.setStatus(PENDING);

        SalesOrderResponse response = SalesOrderResponse.builder().id(3L).status(PENDING).build();

        when(salesOrderRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(order));
        when(salesOrderMapper.toResponse(order)).thenReturn(response);

        assertEquals(List.of(response), salesOrderService.findAll());
    }

    @Test
    void updateShouldAllowPendingOrderEdit() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(10L);

        SalesOrder order = new SalesOrder();
        order.setId(5L);
        order.setStatus(PENDING);
        order.setItems(List.of(createItem(order, product, 2, new BigDecimal("50.00"))));

        SalesOrderResponse response = SalesOrderResponse.builder().id(5L).status(PENDING).build();

        when(salesOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(createInventoryStock(product, 10)));
        doNothing().when(salesOrderMapper).updatePendingOrder(eq(order), eq(request), eq(customer), anyMap());
        when(salesOrderRepository.save(order)).thenReturn(order);
        when(salesOrderMapper.toResponse(order)).thenReturn(response);

        SalesOrderResponse result = salesOrderService.update(5L, request);

        assertEquals(response, result);
        verify(salesOrderRepository).save(order);
    }

    @Test
    void updateShouldReturnOriginalStockAndDeductReplacementStockForPendingOrder() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest replacementItem = new SalesOrderItemRequest();
        replacementItem.setProductId(20L);
        replacementItem.setQuantity(1);
        request.setItems(List.of(replacementItem));

        Customer customer = new Customer();
        customer.setId(1L);

        Product originalProduct = new Product();
        originalProduct.setId(10L);
        originalProduct.setName("Product A");

        Product replacementProduct = new Product();
        replacementProduct.setId(20L);
        replacementProduct.setName("Product B");

        SalesOrder order = new SalesOrder();
        order.setId(5L);
        order.setStatus(PENDING);
        order.setItems(new ArrayList<>(List.of(createItem(order, originalProduct, 2, new BigDecimal("100.00")))));

        when(salesOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(20L)).thenReturn(Optional.of(replacementProduct));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(createInventoryStock(originalProduct, 8)));
        when(inventoryStockRepository.findByProductId(20L)).thenReturn(Optional.of(createInventoryStock(replacementProduct, 7)));
        doAnswer(invocation -> {
            SalesOrder target = invocation.getArgument(0);
            target.getItems().clear();
            target.getItems().add(createItem(target, replacementProduct, 1, new BigDecimal("120.00")));
            return null;
        }).when(salesOrderMapper).updatePendingOrder(eq(order), eq(request), eq(customer), anyMap());
        when(salesOrderRepository.save(order)).thenReturn(order);
        when(stockMovementService.create(any(StockMovementRequest.class))).thenReturn(new StockMovementResponse());

        salesOrderService.update(5L, request);

        verify(stockMovementService).create(argThat(movement ->
                movement.getProductId().equals(10L)
                        && movement.getMovementType() == StockMovementType.IN
                        && movement.getQuantity() == 2));
        verify(stockMovementService).create(argThat(movement ->
                movement.getProductId().equals(20L)
                        && movement.getMovementType() == StockMovementType.OUT
                        && movement.getQuantity() == 1));
    }

    @Test
    void updateShouldRejectNonPendingOrder() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(1L);

        SalesOrderItemRequest itemRequest = new SalesOrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        SalesOrder order = new SalesOrder();
        order.setId(5L);
        order.setStatus(CONFIRMED);

        when(salesOrderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(InvalidSalesOrderStateException.class, () -> salesOrderService.update(5L, request));
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void updateStatusShouldAdvanceOrderLifecycle() {
        SalesOrder order = new SalesOrder();
        order.setId(7L);
        order.setStatus(CONFIRMED);

        SalesOrderResponse shippedResponse = SalesOrderResponse.builder()
                .id(7L)
                .status(SHIPPED)
                .build();

        when(salesOrderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(salesOrderRepository.save(order)).thenReturn(order);
        when(salesOrderMapper.toResponse(order)).thenReturn(shippedResponse);

        SalesOrderResponse result = salesOrderService.updateStatus(7L, SHIPPED);

        assertEquals(SHIPPED, result.getStatus());
        assertEquals(SHIPPED, order.getStatus());
        verify(salesOrderRepository).save(order);
    }

    @Test
    void updateStatusShouldRejectInvalidTransition() {
        SalesOrder order = new SalesOrder();
        order.setId(8L);
        order.setStatus(CANCELLED);

        when(salesOrderRepository.findById(8L)).thenReturn(Optional.of(order));

        assertThrows(InvalidSalesOrderStateException.class, () -> salesOrderService.updateStatus(8L, COMPLETED));
    }

    private InventoryStock createInventoryStock(Product product, int availableQuantity) {
        InventoryStock inventoryStock = new InventoryStock();
        inventoryStock.setProduct(product);
        inventoryStock.setQuantity(availableQuantity);
        inventoryStock.setReservedQuantity(0);
        return inventoryStock;
    }

    private SalesOrderItems createItem(SalesOrder salesOrder, Product product, int quantity, BigDecimal unitPrice) {
        SalesOrderItems item = new SalesOrderItems();
        item.setSalesOrder(salesOrder);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}

