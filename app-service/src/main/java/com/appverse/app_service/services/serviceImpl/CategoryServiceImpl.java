package com.appverse.app_service.services.serviceImpl;


import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appverse.app_service.dto.CategoryRequest;
import com.appverse.app_service.dto.CategoryResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.exception.BadRequestException;
import com.appverse.app_service.exception.CreationException;
import com.appverse.app_service.exception.DatabaseOperationException;
import com.appverse.app_service.exception.DuplicateResourceException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.exception.UpdateOperationException;
import com.appverse.app_service.mapper.CategoryMapper;
import com.appverse.app_service.model.Category;
import com.appverse.app_service.repository.CategoryRepository;
import com.appverse.app_service.services.CategoryService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public MessageResponse createCategory(CategoryRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        if (request.slug() == null || request.slug().isBlank()) {
            throw new BadRequestException("Category slug cannot be empty");
        }

        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Category with this name already exists");
        }

        if (categoryRepository.existsBySlugIgnoreCase(request.slug())) {
            throw new DuplicateResourceException("Category with this slug already exists");
        }

        try {
            Category category = categoryMapper.toEntity(request);

            Category saved = categoryRepository.save(category);
            return new MessageResponse("Category created successfully", saved.getId());

        } catch (RuntimeException ex) {
            throw new CreationException("Failed to create category");
        }
    }

    @Override
    @Transactional
    public MessageResponse updateCategory(String id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (!existing.getName().equalsIgnoreCase(request.name())
                && categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Category with this name already exists");
        }

        if (!existing.getSlug().equalsIgnoreCase(request.slug())
                && categoryRepository.existsBySlugIgnoreCase(request.slug())) {
            throw new DuplicateResourceException("Category with this slug already exists");
        }

        try {
            categoryMapper.updateFromDto(request, existing);

            Category updated = categoryRepository.save(existing);
            return new MessageResponse("Category updated successfully", updated.getId());

        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update category in DB");
        } catch (RuntimeException ex) {
            throw new UpdateOperationException("Unexpected error while updating category");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        try {
            categoryRepository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Database error while deleting category");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }
}
