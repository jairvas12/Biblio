package com.library.category_service.repository;

import com.library.category_service.entity.Category;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.generate-ddl=true"
        }
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void existsByNameIgnoreCase_shouldReturnTrueIgnoringCase() {

        Category category = new Category();
        category.setName("Historia");

        categoryRepository.saveAndFlush(category);

        boolean exists =
                categoryRepository
                        .existsByNameIgnoreCase(
                                "HISTORIA"
                        );

        assertTrue(exists);
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnFalseWhenNameDoesNotExist() {

        boolean exists =
                categoryRepository
                        .existsByNameIgnoreCase(
                                "Ciencia"
                        );

        assertFalse(exists);
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_shouldExcludeCurrentCategory() {

        Category category = new Category();
        category.setName("Historia");

        Category savedCategory =
                categoryRepository.saveAndFlush(
                        category
                );

        boolean exists =
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "historia",
                                savedCategory.getId()
                        );

        assertFalse(exists);
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_shouldDetectDifferentCategory() {

        Category first = new Category();
        first.setName("Historia");

        Category second = new Category();
        second.setName("Ciencia");

        Category savedFirst =
                categoryRepository.saveAndFlush(
                        first
                );

        categoryRepository.saveAndFlush(
                second
        );

        boolean exists =
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "CIENCIA",
                                savedFirst.getId()
                        );

        assertTrue(exists);
    }
}