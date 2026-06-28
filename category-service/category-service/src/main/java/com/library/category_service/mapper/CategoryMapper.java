package com.library.category_service.mapper;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.entity.Category;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(
            CategoryRequestDTO request
    ) {
        Category category = new Category();

        category.setName(
                request.getName()
        );

        return category;
    }

    public CategoryResponseDTO toResponse(
            Category category
    ) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName()
        );
    }
}