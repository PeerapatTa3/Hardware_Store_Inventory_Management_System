package com.hardwarestore.controller.api;

import com.hardwarestore.domain.entity.StockMovementType;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockMovementService stockMovementService;

    @Test
    void findAllShouldReturnMovementList() throws Exception {
        when(stockMovementService.findAll()).thenReturn(List.of(movement(10L, 7)));

        mockMvc.perform(get("/api/v1/stock-movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10))
                .andExpect(jsonPath("$[0].movementType").value("IN"))
                .andExpect(jsonPath("$[0].quantity").value(7));

        verify(stockMovementService).findAll();
    }

    @Test
    void findByProductIdShouldReturnMovementList() throws Exception {
        when(stockMovementService.findByProductId(10L)).thenReturn(List.of(movement(10L, 7)));

        mockMvc.perform(get("/api/v1/stock-movements/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10))
                .andExpect(jsonPath("$[0].quantity").value(7));

        verify(stockMovementService).findByProductId(10L);
    }

    @Test
    void findByProductIdShouldReturnNotFoundForUnknownProduct() throws Exception {
        when(stockMovementService.findByProductId(404L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/stock-movements/products/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void createShouldReturnCreatedMovement() throws Exception {
        when(stockMovementService.create(any())).thenReturn(movement(10L, 7));

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":10,"movementType":"IN","quantity":7,"referenceNo":"PO-10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.movementType").value("IN"));

        verify(stockMovementService).create(any());
    }

    @Test
    void invalidMovementShouldReturnValidationErrorWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":10,"movementType":"IN","quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verifyNoInteractions(stockMovementService);
    }

    private StockMovementResponse movement(Long productId, int quantity) {
        return StockMovementResponse.builder()
                .id(1L)
                .productId(productId)
                .productName("Hammer")
                .movementType(StockMovementType.IN)
                .quantity(quantity)
                .referenceNo("PO-10")
                .build();
    }
}
