package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.dto.request.CustomerRequest;
import com.hardwarestore.dto.response.CustomerResponse;
import com.hardwarestore.exception.DuplicateSkuException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.CustomerMapper;
import com.hardwarestore.repository.CustomerRepository;
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
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createShouldPersistCustomerAndReturnResponse() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Jane Doe");
        request.setPhone("0812345678");
        request.setEmail("jane@example.com");
        request.setAddress("Bangkok");

        Customer customer = new Customer();
        Customer saved = new Customer();
        saved.setId(10L);
        CustomerResponse response = CustomerResponse.builder().id(10L).name("Jane Doe").build();

        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(saved);
        when(customerMapper.toResponse(saved)).thenReturn(response);

        CustomerResponse result = customerService.create(request);

        assertSame(response, result);
        verify(customerRepository).save(customer);
    }

    @Test
    void createShouldFailWhenPhoneAlreadyExists() {
        CustomerRequest request = new CustomerRequest();
        request.setPhone("0812345678");
        request.setEmail("abc@example.com");

        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> customerService.create(request));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.findById(99L));
    }

    @Test
    void findAllShouldMapCustomers() {
        Customer customer = new Customer();
        customer.setId(5L);
        CustomerResponse response = CustomerResponse.builder().id(5L).build();

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        assertEquals(List.of(response), customerService.findAll());
    }
}
