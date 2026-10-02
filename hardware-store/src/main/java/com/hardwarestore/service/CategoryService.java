package com.hardwarestore.service;

import com.hardwarestore.dto.request.CategoryRequest;
import com.hardwarestore.dto.response.CategoryResponse;
import com.hardwarestore.entity.Category;
import com.hardwarestore.exception.DuplicateSkuException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.CategoryMapper;
import com.hardwarestore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateSkuException(request.getName());
        }
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toResponse(saved);
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        Category category = getCategoryOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategoryOrThrow(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = getCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}