package com.library.category_service.service;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO saveCategory(
            CategoryRequestDTO request
    );

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO getCategoryById(
            Long id
    );

    CategoryResponseDTO updateCategory(
            Long id,
            CategoryRequestDTO request
    );

    void deleteCategory(
            Long id
    );
}