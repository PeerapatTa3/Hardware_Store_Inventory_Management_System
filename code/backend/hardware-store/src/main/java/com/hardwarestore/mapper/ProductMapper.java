package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request, Category category, Supplier supplier) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setMinimumStock(request.getMinimumStock());
        product.setCategory(category);
        product.setSupplier(supplier);
        return product;
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .unit(product.getUnit())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .minimumStock(product.getMinimumStock())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .build();
    }
}
