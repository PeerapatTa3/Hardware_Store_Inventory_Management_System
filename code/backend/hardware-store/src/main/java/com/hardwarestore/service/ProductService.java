package com.hardwarestore.service;

import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import com.hardwarestore.dto.response.ProductAdminResponse;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductAdminResponse create(ProductRequest request);

    Page<ProductResponse> findAll(int page, int size, String sortBy, String direction, String keyword, Long categoryId);
    Page<ProductAdminResponse> findAllAdmin(int page, int size, String sortBy, String direction, String keyword, Long categoryId);

    ProductResponse findById(Long id);
    ProductAdminResponse findByIdAdmin(Long id);

    ProductAdminResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
