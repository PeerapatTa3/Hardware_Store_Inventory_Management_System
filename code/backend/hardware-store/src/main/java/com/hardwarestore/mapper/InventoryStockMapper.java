package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.dto.response.InventoryStockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryStockMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    InventoryStockResponse toResponse(InventoryStock inventoryStock);
}
