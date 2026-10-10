package com.hardwarestore.integration;

import com.jayway.jsonpath.JsonPath;
import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.enums.PurchaseOrderStatus;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.repository.CategoryRepository;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.PurchaseOrderRepository;
import com.hardwarestore.repository.StockMovementRepository;
import com.hardwarestore.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class InventoryPurchaseApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryStockRepository inventoryStockRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "OWNER")
    void purchaseReceiveApiShouldPersistOrderStockAndInboundMovement() throws Exception {
        String unique = UUID.randomUUID().toString();
        Category category = new Category();
        category.setName("Integration category " + unique);
        categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("Integration supplier " + unique);
        supplierRepository.save(supplier);

        Product product = new Product();
        product.setSku("INTEGRATION-" + unique);
        product.setName("Integration product");
        product.setUnit("piece");
        product.setPrice(new BigDecimal("15.00"));
        product.setCostPrice(new BigDecimal("8.00"));
        product.setMinimumStock(0);
        product.setCategory(category);
        product.setSupplier(supplier);
        productRepository.save(product);

        InventoryStock stock = new InventoryStock();
        stock.setProduct(product);
        stock.setQuantity(4);
        stock.setReservedQuantity(1);
        inventoryStockRepository.save(stock);

        String request = """
                {"supplierId":%d,"items":[{"productId":%d,"quantity":3,"unitCost":8.00}]}
                """.formatted(supplier.getId(), product.getId());
        String createResponse = mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(24.0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number purchaseIdValue = JsonPath.read(createResponse, "$.id");
        long purchaseId = purchaseIdValue.longValue();
        String purchaseNumber = JsonPath.read(createResponse, "$.purchaseNumber");
        assertTrue(purchaseId > 0);
        assertFalse(purchaseNumber.isBlank());

        mockMvc.perform(post("/api/v1/purchases/{id}/approve", purchaseId))
                .andExpect(status().isOk());

        String receiveRequest = """
                {"items":[{"productId":%d,"receivedQuantity":3}]}
                """.formatted(product.getId());

        mockMvc.perform(post("/api/v1/purchases/{id}/receive", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiveRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        assertEquals(PurchaseOrderStatus.COMPLETED,
                purchaseOrderRepository.findById(purchaseId).orElseThrow().getStatus());
        assertEquals(7, inventoryStockRepository.findByProductId(product.getId())
                .orElseThrow().getQuantity());

        var persistedMovement = stockMovementRepository.findByProductId(
                product.getId(), org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "movementAt"))
                .stream()
                .filter(movement -> purchaseNumber.equals(movement.getReferenceNo()))
                .findFirst()
                .orElseThrow();
        assertEquals(StockMovementType.IN, persistedMovement.getMovementType());
        assertEquals(3, persistedMovement.getQuantity());

        assertMovementAppearsInApi(
                mockMvc.perform(get("/api/v1/stock-movements/products/{productId}", product.getId()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                purchaseNumber);
        assertMovementAppearsInApi(
                mockMvc.perform(get("/api/v1/stock-movements"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                purchaseNumber);
    }

    private void assertMovementAppearsInApi(String response, String purchaseNumber) throws Exception {
        List<?> movements = JsonPath.read(
                response, "$[?(@.referenceNo == '" + purchaseNumber + "')]");
        assertFalse(movements.isEmpty(), "Expected movement for purchase " + purchaseNumber);
    }
}
