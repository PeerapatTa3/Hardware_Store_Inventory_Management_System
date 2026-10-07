package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.entity.SalesOrder.SalesOrderStatus;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.response.SalesOrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderMapperTest {

    private final SalesOrderMapper mapper = new SalesOrderMapper();

    @Test
    void toEntityShouldMapItemsAndCalculateTotals() {
        Customer customer = customer(7L, "Alice");
        Product hammer = product(1L, "Hammer");
        Product wrench = product(2L, "Wrench");

        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(7L);
        request.setItems(List.of(itemRequest(1L, 3, "10.25"), itemRequest(2L, 2, "4.50")));

        SalesOrder order = mapper.toEntity(request, customer, Map.of(1L, hammer, 2L, wrench));

        assertEquals(2, order.getItems().size());
        assertSame(customer, order.getCustomer());
        assertEquals(SalesOrderStatus.PENDING, order.getStatus());
        assertSame(order, order.getItems().get(0).getSalesOrder());
        assertSame(hammer, order.getItems().get(0).getProduct());
        assertEquals(0, new BigDecimal("30.75").compareTo(order.getItems().get(0).getSubtotal()));
        assertEquals(0, new BigDecimal("9.00").compareTo(order.getItems().get(1).getSubtotal()));
        assertEquals(0, new BigDecimal("39.75").compareTo(order.getTotalAmount()));
    }

    @Test
    void toEntityShouldRejectUnresolvedProduct() {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setItems(List.of(itemRequest(99L, 1, "2.00")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.toEntity(request, customer(7L, "Alice"), Map.of()));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void updatePendingOrderShouldReplaceItemsAndRecalculateTotal() {
        Customer originalCustomer = customer(3L, "Old customer");
        Customer updatedCustomer = customer(7L, "Alice");
        Product oldProduct = product(2L, "Old Product");
        Product newProduct = product(1L, "Hammer");

        SalesOrder order = new SalesOrder();
        order.setOrderNumber("SO-KEEP");
        order.setCustomer(originalCustomer);
        order.setStatus(SalesOrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("10.00"));
        order.getItems().add(orderItem(order, oldProduct));

        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(7L);
        request.setItems(List.of(itemRequest(1L, 3, "4.00")));

        mapper.updatePendingOrder(order, request, updatedCustomer, Map.of(1L, newProduct));

        assertEquals("SO-KEEP", order.getOrderNumber());
        assertSame(updatedCustomer, order.getCustomer());
        assertEquals(SalesOrderStatus.PENDING, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertSame(newProduct, order.getItems().get(0).getProduct());
        assertEquals(0, new BigDecimal("12.00").compareTo(order.getTotalAmount()));
    }

    @Test
    void toResponseShouldMapOrderAndItems() {
        Customer customer = customer(7L, "Alice");
        Product product = product(1L, "Hammer");

        SalesOrder order = new SalesOrder();
        order.setId(12L);
        order.setOrderNumber("SO-12");
        order.setCustomer(customer);
        order.setStatus(SalesOrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setItems(List.of(orderItem(order, product)));

        SalesOrderResponse response = mapper.toResponse(order);

        assertEquals(12L, response.getId());
        assertEquals(7L, response.getCustomerId());
        assertEquals("Alice", response.getCustomerName());
        assertEquals(1, response.getItems().size());
        assertEquals("Hammer", response.getItems().get(0).getProductName());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getItems().get(0).getSubtotal()));
    }

    private SalesOrderRequest salesOrderRequest(Long customerId, Long productId, int quantity, String unitPrice) {
        SalesOrderRequest request = new SalesOrderRequest();
        request.setCustomerId(customerId);
        request.setItems(List.of(itemRequest(productId, quantity, unitPrice)));
        return request;
    }

    private SalesOrderItemRequest itemRequest(Long productId, int quantity, String unitPrice) {
        SalesOrderItemRequest request = new SalesOrderItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setUnitPrice(new BigDecimal(unitPrice));
        return request;
    }

    private Customer customer(Long id, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        return customer;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    private SalesOrderItems orderItem(SalesOrder order, Product product) {
        SalesOrderItems item = new SalesOrderItems();
        item.setSalesOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("20.00"));
        return item;
    }
}
