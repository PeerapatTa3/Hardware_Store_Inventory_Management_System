package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Category;
import com.hardwarestore.dto.request.CategoryRequest;
import com.hardwarestore.dto.response.CategoryResponse;
import com.hardwarestore.exception.DuplicateResourceException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.CategoryMapper;
import com.hardwarestore.repository.CategoryRepository;
import com.hardwarestore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(getCategoryOrThrow(id));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategoryOrThrow(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.delete(getCategoryOrThrow(id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}