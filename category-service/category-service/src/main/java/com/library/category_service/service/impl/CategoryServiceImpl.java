package com.library.category_service.service.impl;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.entity.Category;
import com.library.category_service.exception.BusinessException;
import com.library.category_service.exception.ResourceNotFoundException;
import com.library.category_service.mapper.CategoryMapper;
import com.library.category_service.repository.CategoryRepository;
import com.library.category_service.service.CategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log =
            LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponseDTO saveCategory(
            CategoryRequestDTO request
    ) {
        String normalizedName =
                normalizeName(request.getName());

        log.info(
                "Intentando crear categoría con nombre: {}",
                normalizedName
        );

        if (
                categoryRepository.existsByNameIgnoreCase(
                        normalizedName
                )
        ) {
            log.warn(
                    "No se pudo crear la categoría porque el nombre ya existe: {}",
                    normalizedName
            );

            throw new BusinessException(
                    "Ya existe una categoría con el nombre: "
                            + normalizedName
            );
        }

        Category category =
                categoryMapper.toEntity(request);

        category.setName(normalizedName);

        Category savedCategory =
                categoryRepository.save(category);

        log.info(
                "Categoría creada correctamente con id: {}",
                savedCategory.getId()
        );

        return categoryMapper.toResponse(
                savedCategory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {

        log.debug("Consultando todas las categorías");

        List<CategoryResponseDTO> categories =
                categoryRepository.findAll()
                        .stream()
                        .map(categoryMapper::toResponse)
                        .toList();

        log.debug(
                "Se encontraron {} categorías",
                categories.size()
        );

        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(
            Long id
    ) {
        log.debug(
                "Consultando categoría con id: {}",
                id
        );

        Category category =
                findCategoryById(id);

        return categoryMapper.toResponse(
                category
        );
    }

    @Override
    public CategoryResponseDTO updateCategory(
            Long id,
            CategoryRequestDTO request
    ) {
        log.info(
                "Intentando actualizar categoría con id: {}",
                id
        );

        Category existingCategory =
                findCategoryById(id);

        String normalizedName =
                normalizeName(request.getName());

        if (
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                id
                        )
        ) {
            log.warn(
                    "No se pudo actualizar la categoría {} porque el nombre ya existe: {}",
                    id,
                    normalizedName
            );

            throw new BusinessException(
                    "Ya existe una categoría con el nombre: "
                            + normalizedName
            );
        }

        existingCategory.setName(
                normalizedName
        );

        Category updatedCategory =
                categoryRepository.save(
                        existingCategory
                );

        log.info(
                "Categoría actualizada correctamente con id: {}",
                updatedCategory.getId()
        );

        return categoryMapper.toResponse(
                updatedCategory
        );
    }

    @Override
    public void deleteCategory(
            Long id
    ) {
        log.info(
                "Intentando eliminar categoría con id: {}",
                id
        );

        Category existingCategory =
                findCategoryById(id);

        categoryRepository.delete(
                existingCategory
        );

        log.info(
                "Categoría eliminada correctamente con id: {}",
                id
        );
    }

    private Category findCategoryById(
            Long id
    ) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "No se encontró una categoría con id: {}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Categoría no encontrada con id: "
                                    + id
                    );
                });
    }

    private String normalizeName(
            String name
    ) {
        if (
                name == null
                        || name.trim().isEmpty()
        ) {
            log.warn(
                    "Se recibió un nombre de categoría vacío"
            );

            throw new BusinessException(
                    "El nombre de la categoría es obligatorio"
            );
        }

        String normalizedName =
                name.trim();

        if (
                normalizedName.length() < 2
                        || normalizedName.length() > 100
        ) {
            log.warn(
                    "El nombre de categoría tiene una longitud inválida: {} caracteres",
                    normalizedName.length()
            );

            throw new BusinessException(
                    "El nombre de la categoría debe tener entre 2 y 100 caracteres"
            );
        }

        return normalizedName;
    }
}