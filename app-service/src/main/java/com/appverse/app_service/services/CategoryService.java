package com.appverse.app_service.services;

import java.util.List;

import com.appverse.app_service.dto.CategoryRequest;
import com.appverse.app_service.dto.CategoryResponse;
import com.appverse.app_service.dto.MessageResponse;

public interface CategoryService {

    MessageResponse createCategory(CategoryRequest Category);

    MessageResponse updateCategory(String id, CategoryRequest Category);

    void deleteCategory(String id);

    CategoryResponse getCategoryById(String id);

    List<CategoryResponse> getAll();
}
