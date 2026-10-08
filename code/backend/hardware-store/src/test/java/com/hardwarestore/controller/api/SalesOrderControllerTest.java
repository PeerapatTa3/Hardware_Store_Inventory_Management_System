package com.hardwarestore.controller.api;

import com.hardwarestore.domain.entity.SalesOrder.SalesOrderStatus;
import com.hardwarestore.dto.response.SalesOrderItemResponse;
import com.hardwarestore.dto.response.SalesOrderResponse;
import com.hardwarestore.exception.InvalidSalesOrderStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.service.SalesOrderService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalesOrderController.class)
class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesOrderService salesOrderService;

    @Test
    void createShouldReturnCreatedSalesOrder() throws Exception {
        when(salesOrderService.create(any())).thenReturn(order(12L, SalesOrderStatus.PENDING));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":7,"items":[{"productId":4,"quantity":2,"unitPrice":150.00}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(salesOrderService).create(any());
    }

    @Test
    void findAllAndFindByIdShouldReturnSalesOrders() throws Exception {
        when(salesOrderService.findAll()).thenReturn(List.of(order(12L, SalesOrderStatus.PENDING)));
        when(salesOrderService.findById(12L)).thenReturn(order(12L, SalesOrderStatus.PENDING));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12));

        mockMvc.perform(get("/api/v1/orders/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));

        verify(salesOrderService).findAll();
        verify(salesOrderService).findById(12L);
    }

    @Test
    void updateShouldReturnUpdatedSalesOrder() throws Exception {
        when(salesOrderService.update(eq(12L), any())).thenReturn(order(12L, SalesOrderStatus.PENDING));

        mockMvc.perform(put("/api/v1/orders/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":7,"items":[{"productId":4,"quantity":3,"unitPrice":120.00}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));

        verify(salesOrderService).update(eq(12L), any());
    }

    @Test
    void updateStatusShouldReturnUpdatedSalesOrder() throws Exception {
        when(salesOrderService.updateStatus(eq(12L), eq(SalesOrderStatus.SHIPPED)))
                .thenReturn(order(12L, SalesOrderStatus.SHIPPED));

        mockMvc.perform(post("/api/v1/orders/12/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"SHIPPED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(salesOrderService).updateStatus(eq(12L), eq(SalesOrderStatus.SHIPPED));
    }

    @Test
    void invalidRequestShouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":0,"items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verifyNoInteractions(salesOrderService);
    }

    @Test
    void serviceErrorsShouldUseStandardResponses() throws Exception {
        when(salesOrderService.findById(99L)).thenThrow(new ResourceNotFoundException("Sales order not found"));
        when(salesOrderService.updateStatus(12L, SalesOrderStatus.SHIPPED))
                .thenThrow(new InvalidSalesOrderStateException("Only confirmed orders can be marked as shipped"));

        mockMvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/orders/12/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"SHIPPED"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_SALES_ORDER_STATE"));
    }

    @Test
    void illegalStateTransitionsShouldReturnConflict() throws Exception {
        when(salesOrderService.updateStatus(12L, SalesOrderStatus.CANCELLED))
                .thenThrow(new IllegalStateException("Order is already cancelled."));

        mockMvc.perform(post("/api/v1/orders/12/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CANCELLED"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_SALES_ORDER_STATE"));
    }

    private SalesOrderResponse order(Long id, SalesOrderStatus status) {
        return SalesOrderResponse.builder()
                .id(id)
                .orderNumber("SO-" + id)
                .customerId(7L)
                .customerName("Alice")
                .status(status)
                .totalAmount(new BigDecimal("300.00"))
                .createdAt(LocalDateTime.of(2025, 1, 1, 9, 0))
                .items(List.of(SalesOrderItemResponse.builder()
                        .id(1L)
                        .productId(4L)
                        .productName("Hammer")
                        .quantity(2)
                        .unitPrice(new BigDecimal("150.00"))
                        .subtotal(new BigDecimal("300.00"))
                        .build()))
                .build();
    }
}
