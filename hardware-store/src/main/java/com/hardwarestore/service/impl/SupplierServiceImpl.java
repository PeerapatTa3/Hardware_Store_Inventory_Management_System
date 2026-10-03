package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.SupplierRequest;
import com.hardwarestore.dto.response.SupplierResponse;
import com.hardwarestore.exception.DuplicateSkuException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.SupplierMapper;
import com.hardwarestore.repository.SupplierRepository;
import com.hardwarestore.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponse create(SupplierRequest request) {
        if (request.getName() != null && supplierRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateSkuException(request.getName());
        }

        if (request.getEmail() != null && supplierRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateSkuException(request.getEmail());
        }

        if (request.getPhone() != null && supplierRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateSkuException(request.getPhone());
        }

        Supplier saved = supplierRepository.save(supplierMapper.toEntity(request));
        return supplierMapper.toResponse(saved);
    }

    @Override
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    public SupplierResponse findById(Long id) {
        return supplierMapper.toResponse(getSupplierOrThrow(id));
    }

    @Override
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = getSupplierOrThrow(id);
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) {
        supplierRepository.delete(getSupplierOrThrow(id));
    }

    private Supplier getSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }
}
