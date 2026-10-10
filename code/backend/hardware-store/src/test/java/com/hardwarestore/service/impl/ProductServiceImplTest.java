package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductAdminResponse;
import com.hardwarestore.dto.response.ProductResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.ProductMapper;
import com.hardwarestore.repository.CategoryRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createShouldSaveAndReturnResponse() {
        ProductRequest request = new ProductRequest();
        request.setSku("DRILL-001");
        request.setName("Cordless Drill");
        request.setDescription("Battery drill");
        request.setUnit("pcs");
        request.setPrice(new BigDecimal("2590.00"));
        request.setCostPrice(new BigDecimal("1900.00"));
        request.setMinimumStock(5);
        request.setCategoryId(1L);
        request.setSupplierId(2L);

        Category category = new Category();
        category.setId(1L);
        category.setName("Tool");

        Supplier supplier = new Supplier();
        supplier.setId(2L);
        supplier.setName("ABC Supply");

        Product entity = new Product();
        entity.setId(10L);
        entity.setSku("DRILL-001");
        entity.setName("Cordless Drill");
        entity.setDescription("Battery drill");
        entity.setUnit("pcs");
        entity.setPrice(new BigDecimal("2590.00"));
        entity.setCostPrice(new BigDecimal("1900.00"));
        entity.setMinimumStock(5);
        entity.setCategory(category);
        entity.setSupplier(supplier);

        ProductAdminResponse response = ProductAdminResponse.builder()
                .id(10L)
                .sku("DRILL-001")
                .name("Cordless Drill")
                .description("Battery drill")
                .unit("pcs")
                .price(new BigDecimal("2590.00"))
                .costPrice(new BigDecimal("1900.00"))
                .minimumStock(5)
                .categoryId(1L)
                .supplierId(2L)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(productRepository.existsBySkuIgnoreCase("DRILL-001")).thenReturn(false);
        when(productMapper.toEntity(request, category, supplier)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toAdminResponse(entity)).thenReturn(response);

        ProductAdminResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals("DRILL-001", result.getSku());
        verify(productRepository).save(entity);
    }

    @Test
    void findByIdShouldThrowWhenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void findAllShouldReturnPagedProducts() {
        Product entity = new Product();
        entity.setId(1L);
        entity.setSku("ABC-1");
        entity.setName("Hammer");
        entity.setUnit("pcs");
        entity.setPrice(new BigDecimal("150.00"));
        entity.setCostPrice(new BigDecimal("100.00"));
        entity.setMinimumStock(3);

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .sku("ABC-1")
                .name("Hammer")
                .unit("pcs")
                .price(new BigDecimal("150.00"))
                .build();

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Product> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toResponse(entity)).thenReturn(response);

        Page<ProductResponse> result = productService.findAll(0, 10, "id", "asc", null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Hammer", result.getContent().get(0).getName());
    }
}
