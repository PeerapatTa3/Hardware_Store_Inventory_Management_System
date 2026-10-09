package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.dto.request.CustomerRequest;
import com.hardwarestore.dto.response.CustomerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRequest request);
    CustomerResponse toResponse(Customer customer);
}
