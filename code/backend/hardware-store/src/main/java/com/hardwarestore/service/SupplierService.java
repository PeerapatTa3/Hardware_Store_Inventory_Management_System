package com.hardwarestore.service;

import com.hardwarestore.dto.request.SupplierRequest;
import com.hardwarestore.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {

    SupplierResponse create(SupplierRequest request);

    List<SupplierResponse> findAll();

    SupplierResponse findById(Long id);

    SupplierResponse update(Long id, SupplierRequest request);

    void delete(Long id);
}
