package com.library.book_service.client;

import com.library.book_service.config.FeignAuthConfig;
import com.library.book_service.dto.CategoryResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "category-service",
        configuration = FeignAuthConfig.class
)
public interface CategoryClient {

    @GetMapping("/categories/{id}")
    CategoryResponseDTO getCategoryById(
            @PathVariable("id") Long id
    );

    @GetMapping("/categories")
    List<CategoryResponseDTO> getAllCategories();
}