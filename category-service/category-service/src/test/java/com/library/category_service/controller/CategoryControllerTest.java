package com.library.category_service.controller;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.exception.GlobalExceptionHandler;
import com.library.category_service.exception.ResourceNotFoundException;
import com.library.category_service.service.CategoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        categoryService =
                Mockito.mock(CategoryService.class);

        CategoryController controller =
                new CategoryController(categoryService);

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();
    }

    @Test
    void saveCategory_shouldReturnCreated()
            throws Exception {

        CategoryResponseDTO response =
                new CategoryResponseDTO(
                        1L,
                        "Historia"
                );

        when(
                categoryService.saveCategory(
                        any(CategoryRequestDTO.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/categories")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Historia"
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Historia")
                );
    }

    @Test
    void saveCategory_shouldReturnBadRequestWhenNameIsBlank()
            throws Exception {

        mockMvc.perform(
                        post("/categories")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": ""
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error")
                                .value("VALIDATION ERROR")
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.name"
                        ).exists()
                );
    }

    @Test
    void getAllCategories_shouldReturnOk()
            throws Exception {

        when(
                categoryService.getAllCategories()
        ).thenReturn(
                List.of(
                        new CategoryResponseDTO(
                                1L,
                                "Historia"
                        ),
                        new CategoryResponseDTO(
                                2L,
                                "Ciencia"
                        )
                )
        );

        mockMvc.perform(
                        get("/categories")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Historia")
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Ciencia")
                );
    }

    @Test
    void getCategoryById_shouldReturnOk()
            throws Exception {

        when(
                categoryService.getCategoryById(1L)
        ).thenReturn(
                new CategoryResponseDTO(
                        1L,
                        "Historia"
                )
        );

        mockMvc.perform(
                        get("/categories/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Historia")
                );
    }

    @Test
    void getCategoryById_shouldReturnNotFound()
            throws Exception {

        when(
                categoryService.getCategoryById(99L)
        ).thenThrow(
                new ResourceNotFoundException(
                        "Categoría no encontrada con id: 99"
                )
        );

        mockMvc.perform(
                        get("/categories/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.error")
                                .value("NOT FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Categoría no encontrada con id: 99"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/categories/99")
                );
    }

    @Test
    void updateCategory_shouldReturnOk()
            throws Exception {

        when(
                categoryService.updateCategory(
                        eq(1L),
                        any(CategoryRequestDTO.class)
                )
        ).thenReturn(
                new CategoryResponseDTO(
                        1L,
                        "Literatura"
                )
        );

        mockMvc.perform(
                        put("/categories/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Literatura"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Literatura")
                );
    }

    @Test
    void deleteCategory_shouldReturnNoContent()
            throws Exception {

        doNothing()
                .when(categoryService)
                .deleteCategory(1L);

        mockMvc.perform(
                        delete("/categories/1")
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}