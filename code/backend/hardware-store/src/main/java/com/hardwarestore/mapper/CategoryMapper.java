package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.dto.request.CategoryRequest;
import com.hardwarestore.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);
    CategoryResponse toResponse(Category category);
}
