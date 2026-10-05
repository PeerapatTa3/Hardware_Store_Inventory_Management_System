package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseItem;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.entity.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseItemRequest;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.mapper.PurchaseOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(PurchaseOrderMapper.class)
class PurchasePersistenceTest {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePurchaseOrderShouldPersistStatusSupplierAndItems() {
        Category category = new Category();
        category.setName("Tools");
        categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("Tool Supplier");
        supplierRepository.save(supplier);

        Product product = new Product();
        product.setSku("TOOL-001");
        product.setName("Hammer");
        product.setUnit("piece");
        product.setPrice(new BigDecimal("20.00"));
        product.setCostPrice(new BigDecimal("10.00"));
        product.setMinimumStock(2);
        product.setCategory(category);
        product.setSupplier(supplier);
        productRepository.save(product);

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNumber("PO-1001");
        order.setSupplier(supplier);
        order.setTotalAmount(new BigDecimal("30.00"));

        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setUnitCost(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("30.00"));
        order.getItems().add(item);

        PurchaseOrder saved = purchaseOrderRepository.saveAndFlush(order);
        PurchaseOrder loaded = purchaseOrderRepository.findById(saved.getId()).orElseThrow();

        assertEquals(PurchaseOrderStatus.PENDING, loaded.getStatus());
        assertNotNull(loaded.getCreatedAt());
        assertEquals("Tool Supplier", loaded.getSupplier().getName());
        assertEquals(1, loaded.getItems().size());
        assertEquals("Hammer", loaded.getItems().get(0).getProduct().getName());
        assertEquals(0, new BigDecimal("30.00").compareTo(loaded.getItems().get(0).getSubtotal()));
    }

    @Test
    void updatePendingOrderShouldReplacePurchaseItemsInDatabase() {
        Category category = new Category();
        category.setName("Tools");
        categoryRepository.save(category);

        Supplier originalSupplier = new Supplier();
        originalSupplier.setName("Original Supplier");
        supplierRepository.save(originalSupplier);
        Supplier updatedSupplier = new Supplier();
        updatedSupplier.setName("Updated Supplier");
        supplierRepository.save(updatedSupplier);

        Product oldProduct = product("OLD-001", "Old Product", category, originalSupplier);
        Product newProduct = product("NEW-001", "New Product", category, updatedSupplier);
        productRepository.saveAll(List.of(oldProduct, newProduct));

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNumber("PO-UPDATE");
        order.setSupplier(originalSupplier);
        order.setTotalAmount(new BigDecimal("10.00"));
        PurchaseItem oldItem = new PurchaseItem();
        oldItem.setPurchaseOrder(order);
        oldItem.setProduct(oldProduct);
        oldItem.setQuantity(1);
        oldItem.setUnitCost(new BigDecimal("10.00"));
        oldItem.setSubtotal(new BigDecimal("10.00"));
        order.getItems().add(oldItem);
        PurchaseOrder saved = purchaseOrderRepository.saveAndFlush(order);

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(updatedSupplier.getId());
        PurchaseItemRequest newItem = new PurchaseItemRequest();
        newItem.setProductId(newProduct.getId());
        newItem.setQuantity(2);
        newItem.setUnitCost(new BigDecimal("6.00"));
        request.setItems(List.of(newItem));

        purchaseOrderMapper.updatePendingOrder(saved, request, updatedSupplier,
                Map.of(newProduct.getId(), newProduct));
        purchaseOrderRepository.saveAndFlush(saved);
        entityManager.clear();

        PurchaseOrder loaded = purchaseOrderRepository.findById(saved.getId()).orElseThrow();
        assertEquals(PurchaseOrderStatus.PENDING, loaded.getStatus());
        assertEquals("PO-UPDATE", loaded.getPurchaseNumber());
        assertEquals("Updated Supplier", loaded.getSupplier().getName());
        assertEquals(1, loaded.getItems().size());
        assertEquals("New Product", loaded.getItems().get(0).getProduct().getName());
        assertEquals(0, new BigDecimal("12.00").compareTo(loaded.getTotalAmount()));
        assertEquals(1, purchaseItemRepository.count());
    }

    private Product product(String sku, String name, Category category, Supplier supplier) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setUnit("piece");
        product.setPrice(new BigDecimal("20.00"));
        product.setCostPrice(new BigDecimal("10.00"));
        product.setMinimumStock(1);
        product.setCategory(category);
        product.setSupplier(supplier);
        return product;
    }
}
