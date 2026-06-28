package com.library.category_service.service.impl;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.entity.Category;
import com.library.category_service.exception.BusinessException;
import com.library.category_service.exception.ResourceNotFoundException;
import com.library.category_service.mapper.CategoryMapper;
import com.library.category_service.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(
                categoryRepository,
                new CategoryMapper()
        );
    }

    @Test
    void saveCategory_shouldNormalizeAndSaveCategory() {
        CategoryRequestDTO request =
                createRequest("  Historia  ");

        when(
                categoryRepository.existsByNameIgnoreCase(
                        "Historia"
                )
        ).thenReturn(false);

        when(
                categoryRepository.save(any(Category.class))
        ).thenAnswer(invocation -> {
            Category category =
                    invocation.getArgument(0);

            category.setId(1L);

            return category;
        });

        CategoryResponseDTO response =
                categoryService.saveCategory(request);

        assertEquals(1L, response.getId());
        assertEquals("Historia", response.getName());

        ArgumentCaptor<Category> captor =
                ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).save(
                captor.capture()
        );

        assertEquals(
                "Historia",
                captor.getValue().getName()
        );
    }

    @Test
    void saveCategory_shouldThrowBusinessExceptionWhenNameExists() {
        CategoryRequestDTO request =
                createRequest("Historia");

        when(
                categoryRepository.existsByNameIgnoreCase(
                        "Historia"
                )
        ).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> categoryService.saveCategory(request)
        );

        verify(
                categoryRepository,
                never()
        ).save(any(Category.class));
    }

    @Test
    void saveCategory_shouldThrowBusinessExceptionWhenNameIsBlank() {
        CategoryRequestDTO request =
                createRequest("   ");

        assertThrows(
                BusinessException.class,
                () -> categoryService.saveCategory(request)
        );

        verify(
                categoryRepository,
                never()
        ).save(any(Category.class));
    }

    @Test
    void saveCategory_shouldThrowBusinessExceptionWhenNameIsTooShort() {
        CategoryRequestDTO request =
                createRequest("A");

        assertThrows(
                BusinessException.class,
                () -> categoryService.saveCategory(request)
        );

        verify(
                categoryRepository,
                never()
        ).save(any(Category.class));
    }

    @Test
    void getAllCategories_shouldReturnMappedCategories() {
        Category first =
                createCategory(1L, "Historia");

        Category second =
                createCategory(2L, "Ciencia");

        when(
                categoryRepository.findAll()
        ).thenReturn(
                List.of(first, second)
        );

        List<CategoryResponseDTO> response =
                categoryService.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Historia", response.get(0).getName());
        assertEquals("Ciencia", response.get(1).getName());
    }

    @Test
    void getCategoryById_shouldReturnCategoryWhenItExists() {
        Category category =
                createCategory(1L, "Historia");

        when(
                categoryRepository.findById(1L)
        ).thenReturn(Optional.of(category));

        CategoryResponseDTO response =
                categoryService.getCategoryById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Historia", response.getName());
    }

    @Test
    void getCategoryById_shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(
                categoryRepository.findById(99L)
        ).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                categoryService.getCategoryById(
                                        99L
                                )
                );

        assertEquals(
                "Categoría no encontrada con id: 99",
                exception.getMessage()
        );
    }

    @Test
    void updateCategory_shouldNormalizeAndUpdateCategory() {
        Category existingCategory =
                createCategory(1L, "Historia");

        CategoryRequestDTO request =
                createRequest("  Literatura  ");

        when(
                categoryRepository.findById(1L)
        ).thenReturn(Optional.of(existingCategory));

        when(
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "Literatura",
                                1L
                        )
        ).thenReturn(false);

        when(
                categoryRepository.save(existingCategory)
        ).thenReturn(existingCategory);

        CategoryResponseDTO response =
                categoryService.updateCategory(
                        1L,
                        request
                );

        assertEquals(1L, response.getId());
        assertEquals(
                "Literatura",
                response.getName()
        );
    }

    @Test
    void updateCategory_shouldThrowBusinessExceptionWhenNameExists() {
        Category existingCategory =
                createCategory(1L, "Historia");

        CategoryRequestDTO request =
                createRequest("Ciencia");

        when(
                categoryRepository.findById(1L)
        ).thenReturn(Optional.of(existingCategory));

        when(
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "Ciencia",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () ->
                        categoryService.updateCategory(
                                1L,
                                request
                        )
        );

        verify(
                categoryRepository,
                never()
        ).save(any(Category.class));
    }

    @Test
    void deleteCategory_shouldDeleteExistingCategory() {
        Category category =
                createCategory(1L, "Historia");

        when(
                categoryRepository.findById(1L)
        ).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    private CategoryRequestDTO createRequest(
            String name
    ) {
        CategoryRequestDTO request =
                new CategoryRequestDTO();

        request.setName(name);

        return request;
    }

    private Category createCategory(
            Long id,
            String name
    ) {
        Category category = new Category();

        category.setId(id);
        category.setName(name);

        return category;
    }
}