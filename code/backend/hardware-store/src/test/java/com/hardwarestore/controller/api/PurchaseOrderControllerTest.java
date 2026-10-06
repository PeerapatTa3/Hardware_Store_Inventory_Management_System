package com.hardwarestore.controller.api;

import com.hardwarestore.domain.entity.PurchaseOrderStatus;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import com.hardwarestore.exception.InvalidPurchaseStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.service.PurchaseOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @Test
    void createShouldReturnCreatedPurchase() throws Exception {
        when(purchaseOrderService.create(any())).thenReturn(purchase(17L, PurchaseOrderStatus.PENDING));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"supplierId":3,"items":[{"productId":4,"quantity":2,"unitCost":12.50}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(25.0));

        verify(purchaseOrderService).create(any());
    }

    @Test
    void findAllAndFindByIdShouldReturnPurchaseResponses() throws Exception {
        when(purchaseOrderService.findAll()).thenReturn(List.of(
                purchase(17L, PurchaseOrderStatus.PENDING)));
        when(purchaseOrderService.findById(17L)).thenReturn(
                purchase(17L, PurchaseOrderStatus.PENDING));

        mockMvc.perform(get("/api/v1/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(17))
                .andExpect(jsonPath("$[0].purchaseNumber").value("PO-17"));

        mockMvc.perform(get("/api/v1/purchases/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17));

        verify(purchaseOrderService).findAll();
        verify(purchaseOrderService).findById(17L);
    }

    @Test
    void updateShouldReturnUpdatedPurchase() throws Exception {
        when(purchaseOrderService.update(eq(17L), any())).thenReturn(
                purchase(17L, PurchaseOrderStatus.PENDING));

        mockMvc.perform(put("/api/v1/purchases/17")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"supplierId":3,"items":[{"productId":4,"quantity":2,"unitCost":12.50}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(purchaseOrderService).update(eq(17L), any());
    }

    @Test
    void receiveShouldReturnCompletedPurchase() throws Exception {
        when(purchaseOrderService.receive(17L)).thenReturn(
                purchase(17L, PurchaseOrderStatus.COMPLETED));

        mockMvc.perform(post("/api/v1/purchases/17/receive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(purchaseOrderService).receive(17L);
    }

    @Test
    void invalidRequestShouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"supplierId":0,"items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/v1/purchases"));

        mockMvc.perform(put("/api/v1/purchases/17")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"supplierId":3,"items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/v1/purchases/17"));

        verifyNoInteractions(purchaseOrderService);
    }

    @Test
    void serviceErrorsShouldUseStandardNotFoundAndConflictResponses() throws Exception {
        when(purchaseOrderService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Purchase not found"));
        when(purchaseOrderService.receive(17L))
                .thenThrow(new InvalidPurchaseStateException("Only pending purchases can be received"));

        mockMvc.perform(get("/api/v1/purchases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/purchases/17/receive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_PURCHASE_STATE"));
    }

    private PurchaseOrderResponse purchase(Long id, PurchaseOrderStatus status) {
        return PurchaseOrderResponse.builder()
                .id(id)
                .purchaseNumber("PO-" + id)
                .supplierId(3L)
                .supplierName("Supplier")
                .status(status)
                .totalAmount(new BigDecimal("25.00"))
                .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .items(List.of())
                .build();
    }
}
