package com.hardwarestore.service;

import com.hardwarestore.dto.request.CustomerRequest;
import com.hardwarestore.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    List<CustomerResponse> findAll();

    CustomerResponse findById(Long id);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
