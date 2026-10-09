package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import com.hardwarestore.dto.response.ProductAdminResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "supplier", source = "supplier")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    Product toEntity(ProductRequest request, Category category, Supplier supplier);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "supplier.id", target = "supplierId")
    ProductResponse toResponse(Product product);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "supplier.id", target = "supplierId")
    ProductAdminResponse toAdminResponse(Product product);
}
