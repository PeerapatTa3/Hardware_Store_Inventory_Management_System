package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.SupplierRequest;
import com.hardwarestore.dto.response.SupplierResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toEntity(SupplierRequest request);
    SupplierResponse toResponse(Supplier supplier);
}
