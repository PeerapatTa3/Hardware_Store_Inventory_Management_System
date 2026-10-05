package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.dto.request.CategoryRequest;
import com.hardwarestore.dto.response.CategoryResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.CategoryMapper;
import com.hardwarestore.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createShouldSaveAndReturnResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Drill");
        request.setDescription("Power tool");

        Category entity = new Category();
        entity.setId(1L);
        entity.setName("Drill");
        entity.setDescription("Power tool");

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Drill")
                .description("Power tool")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Drill")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toResponse(entity)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        verify(categoryRepository).save(entity);
    }

    @Test
    void findAllShouldReturnMappedCategories() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Safety");
        category.setDescription("Protection gear");

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Safety")
                .description("Protection gear")
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        List<CategoryResponse> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Safety", result.get(0).getName());
    }

    @Test
    void findByIdShouldThrowWhenCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }
}
