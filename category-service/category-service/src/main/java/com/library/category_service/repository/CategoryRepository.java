package com.library.category_service.repository;

import com.library.category_service.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );
}