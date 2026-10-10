package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseItem;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.enums.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseItemRequest;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderMapperTest {

    private final PurchaseOrderMapper mapper = org.mapstruct.factory.Mappers.getMapper(PurchaseOrderMapper.class);

    @Test
    void toEntityShouldMapItemsAndCalculateTotals() {
        Supplier supplier = new Supplier();
        supplier.setId(7L);
        Product hammer = product(1L, "Hammer");
        Product wrench = product(2L, "Wrench");

        PurchaseItemRequest first = itemRequest(1L, 3, "10.25");
        PurchaseItemRequest second = itemRequest(2L, 2, "4.50");
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(7L);
        request.setItems(List.of(first, second));

        PurchaseOrder order = mapper.toEntity(
                request, "PO-1001", supplier, Map.of(1L, hammer, 2L, wrench));

        assertEquals("PO-1001", order.getPurchaseNumber());
        assertSame(supplier, order.getSupplier());
        assertEquals(PurchaseOrderStatus.PENDING, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertSame(order, order.getItems().get(0).getPurchaseOrder());
        assertSame(hammer, order.getItems().get(0).getProduct());
        assertEquals(0, new BigDecimal("30.75").compareTo(order.getItems().get(0).getSubtotal()));
        assertEquals(0, new BigDecimal("9.00").compareTo(order.getItems().get(1).getSubtotal()));
        assertEquals(0, new BigDecimal("39.75").compareTo(order.getTotalAmount()));
    }

    @Test
    void toEntityShouldRejectUnresolvedProduct() {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setItems(List.of(itemRequest(99L, 1, "2.00")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.toEntity(request, "PO-1002", new Supplier(), Map.of()));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void toResponseShouldMapOrderAndItems() {
        Supplier supplier = new Supplier();
        supplier.setId(7L);
        supplier.setName("Tool Supplier");
        Product product = product(1L, "Hammer");

        PurchaseOrder order = new PurchaseOrder();
        order.setId(12L);
        order.setPurchaseNumber("PO-1001");
        order.setSupplier(supplier);
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setItems(List.of(purchaseItem(order, product)));

        PurchaseOrderResponse response = mapper.toResponse(order);

        assertEquals(12L, response.getId());
        assertEquals(7L, response.getSupplierId());
        assertEquals("Tool Supplier", response.getSupplierName());
        assertEquals(1, response.getItems().size());
        assertEquals("Hammer", response.getItems().get(0).getProductName());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getItems().get(0).getSubtotal()));
    }

    @Test
    void updatePendingOrderShouldReplaceItemsAndRecalculateTotal() {
        Supplier originalSupplier = new Supplier();
        originalSupplier.setId(3L);
        Supplier updatedSupplier = new Supplier();
        updatedSupplier.setId(7L);
        Product oldProduct = product(2L, "Old product");
        Product newProduct = product(1L, "Hammer");
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNumber("PO-KEEP");
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setSupplier(originalSupplier);
        order.setTotalAmount(new BigDecimal("10.00"));
        order.getItems().add(purchaseItem(order, oldProduct));

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(7L);
        request.setItems(List.of(itemRequest(1L, 3, "4.00")));

        mapper.updatePendingOrder(order, request, updatedSupplier, Map.of(1L, newProduct));

        assertEquals("PO-KEEP", order.getPurchaseNumber());
        assertEquals(PurchaseOrderStatus.PENDING, order.getStatus());
        assertSame(updatedSupplier, order.getSupplier());
        assertEquals(1, order.getItems().size());
        assertSame(order, order.getItems().get(0).getPurchaseOrder());
        assertSame(newProduct, order.getItems().get(0).getProduct());
        assertEquals(0, new BigDecimal("12.00").compareTo(order.getTotalAmount()));
    }

    private PurchaseItemRequest itemRequest(Long productId, int quantity, String unitCost) {
        PurchaseItemRequest request = new PurchaseItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setUnitCost(new BigDecimal(unitCost));
        return request;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    private PurchaseItem purchaseItem(PurchaseOrder order, Product product) {
        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitCost(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("20.00"));
        return item;
    }
}
