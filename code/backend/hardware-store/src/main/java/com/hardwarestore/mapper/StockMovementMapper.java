package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.dto.response.StockMovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    StockMovementResponse toResponse(StockMovement stockMovement);
}
