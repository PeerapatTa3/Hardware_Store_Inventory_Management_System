package com.hardwarestore.controller.api;

import com.hardwarestore.dto.response.InventoryStockResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.service.InventoryStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryStockService inventoryStockService;

    @Test
    void getStockShouldReturnInventoryResponse() throws Exception {
        when(inventoryStockService.getStockByProductId(10L)).thenReturn(inventory(10L, 12, 2));

        mockMvc.perform(get("/api/v1/inventory/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.availableQuantity").value(10));

        verify(inventoryStockService).getStockByProductId(10L);
    }

    @Test
    void adjustStockShouldReturnUpdatedInventoryResponse() throws Exception {
        when(inventoryStockService.adjustStock(eq(10L), any())).thenReturn(inventory(10L, 15, 2));

        mockMvc.perform(put("/api/v1/inventory/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":15,"reason":"Stock count"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(15))
                .andExpect(jsonPath("$.availableQuantity").value(13));

        verify(inventoryStockService).adjustStock(eq(10L), any());
    }

    @Test
    void invalidAdjustmentShouldReturnValidationErrorWithoutCallingService() throws Exception {
        mockMvc.perform(put("/api/v1/inventory/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":null,"reason":"Stock count"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verifyNoInteractions(inventoryStockService);
    }

    @Test
    void missingProductOrInventoryShouldReturnNotFound() throws Exception {
        when(inventoryStockService.getStockByProductId(404L))
                .thenThrow(new ResourceNotFoundException("Inventory not found"));

        mockMvc.perform(get("/api/v1/inventory/products/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private InventoryStockResponse inventory(Long productId, int quantity, int reservedQuantity) {
        return InventoryStockResponse.builder()
                .id(1L)
                .productId(productId)
                .productName("Hammer")
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(quantity - reservedQuantity)
                .build();
    }
}
