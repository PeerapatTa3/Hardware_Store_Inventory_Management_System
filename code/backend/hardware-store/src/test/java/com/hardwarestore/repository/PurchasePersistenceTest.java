package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseItem;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.entity.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PurchasePersistenceTest {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

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
}
