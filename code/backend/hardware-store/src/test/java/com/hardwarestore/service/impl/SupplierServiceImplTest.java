package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.SupplierRequest;
import com.hardwarestore.dto.response.SupplierResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.SupplierMapper;
import com.hardwarestore.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createShouldSaveAndReturnResponse() {
        SupplierRequest request = new SupplierRequest();
        request.setName("ABC Tool");
        request.setPhone("0812345678");
        request.setEmail("abc@example.com");
        request.setAddress("Bangkok");

        Supplier entity = new Supplier();
        entity.setId(1L);
        entity.setName("ABC Tool");
        entity.setPhone("0812345678");
        entity.setEmail("abc@example.com");
        entity.setAddress("Bangkok");

        SupplierResponse response = SupplierResponse.builder()
                .id(1L)
                .name("ABC Tool")
                .phone("0812345678")
                .email("abc@example.com")
                .address("Bangkok")
                .build();

        when(supplierRepository.existsByNameIgnoreCase("ABC Tool")).thenReturn(false);
        when(supplierRepository.existsByEmailIgnoreCase("abc@example.com")).thenReturn(false);
        when(supplierRepository.existsByPhone("0812345678")).thenReturn(false);
        when(supplierMapper.toEntity(request)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(entity);
        when(supplierMapper.toResponse(entity)).thenReturn(response);

        SupplierResponse result = supplierService.create(request);

        assertNotNull(result);
        assertEquals("ABC Tool", result.getName());
        verify(supplierRepository).save(entity);
    }

    @Test
    void findAllShouldReturnMappedSuppliers() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("XYZ Supply");
        supplier.setPhone("0899999999");
        supplier.setEmail("xyz@example.com");
        supplier.setAddress("Chiang Mai");

        SupplierResponse response = SupplierResponse.builder()
                .id(1L)
                .name("XYZ Supply")
                .phone("0899999999")
                .email("xyz@example.com")
                .address("Chiang Mai")
                .build();

        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        List<SupplierResponse> result = supplierService.findAll();

        assertEquals(1, result.size());
        assertEquals("XYZ Supply", result.get(0).getName());
    }

    @Test
    void findByIdShouldThrowWhenSupplierMissing() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.findById(99L));
    }
}
