package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import com.hardwarestore.exception.DuplicateSkuException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.ProductMapper;
import com.hardwarestore.repository.CategoryRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.SupplierRepository;
import com.hardwarestore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        Product product = productMapper.toEntity(request, category, supplier);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public Page<ProductResponse> findAll(int page, int size, String sortBy, String direction, String keyword, Long categoryId) {
        String effectiveSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, effectiveSortBy));

        Page<Product> productPage;
        if (keyword != null && !keyword.isBlank() && categoryId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse findById(Long id) {
        return productMapper.toResponse(getProductOrThrow(id));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProductOrThrow(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setMinimumStock(request.getMinimumStock());
        product.setCategory(category);
        product.setSupplier(supplier);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        productRepository.delete(getProductOrThrow(id));
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
}
