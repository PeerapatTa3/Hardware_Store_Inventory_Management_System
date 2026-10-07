package com.hardwarestore.controller.api;

import com.hardwarestore.dto.response.CustomerResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void createShouldReturnCreatedCustomer() throws Exception {
        when(customerService.create(any())).thenReturn(customer(7L, "Alice"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","phone":"0812345678","email":"alice@example.com","address":"Bangkok"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(customerService).create(any());
    }

    @Test
    void findAllAndFindByIdShouldReturnCustomers() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(customer(7L, "Alice")));
        when(customerService.findById(7L)).thenReturn(customer(7L, "Alice"));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Alice"));

        mockMvc.perform(get("/api/v1/customers/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(customerService).findAll();
        verify(customerService).findById(7L);
    }

    @Test
    void updateShouldReturnUpdatedCustomer() throws Exception {
        when(customerService.update(eq(7L), any())).thenReturn(customer(7L, "Alice Updated"));

        mockMvc.perform(put("/api/v1/customers/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice Updated","phone":"0812345678","email":"alice@example.com","address":"Chiang Mai"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Alice Updated"));

        verify(customerService).update(eq(7L), any());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(customerService).delete(7L);

        mockMvc.perform(delete("/api/v1/customers/7"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(7L);
    }

    @Test
    void invalidRequestShouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","phone":"","email":"bad-email","address":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verifyNoInteractions(customerService);
    }

    @Test
    void serviceErrorsShouldUseStandardNotFoundResponse() throws Exception {
        when(customerService.findById(99L)).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private CustomerResponse customer(Long id, String name) {
        return CustomerResponse.builder()
                .id(id)
                .name(name)
                .phone("0812345678")
                .email("alice@example.com")
                .address("Bangkok")
                .build();
    }
}
