package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.dto.request.CustomerRequest;
import com.hardwarestore.dto.response.CustomerResponse;
import com.hardwarestore.exception.DuplicateResourceException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.CustomerMapper;
import com.hardwarestore.repository.CustomerRepository;
import com.hardwarestore.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.getPhone());
        }

        if (request.getEmail() != null && customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        Customer saved = customerRepository.save(customerMapper.toEntity(request));
        return customerMapper.toResponse(saved);
    }

    @Override
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse findById(Long id) {
        return customerMapper.toResponse(getCustomerOrThrow(id));
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getCustomerOrThrow(id);

        if (request.getPhone() != null && !request.getPhone().equalsIgnoreCase(customer.getPhone())
                && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(customer.getEmail())
                && customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public void delete(Long id) {
        customerRepository.delete(getCustomerOrThrow(id));
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}

