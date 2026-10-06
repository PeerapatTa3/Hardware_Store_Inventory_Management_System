package com.hardwarestore.service;

import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    Page<ProductResponse> findAll(int page, int size, String sortBy, String direction, String keyword, Long categoryId);

    ProductResponse findById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
