package com.library.book_service.service.impl;

import com.library.book_service.client.CategoryClient;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;
import com.library.book_service.dto.CategoryResponseDTO;

import com.library.book_service.entity.Book;

import com.library.book_service.exception.BusinessException;
import com.library.book_service.exception.RemoteServiceException;
import com.library.book_service.exception.ResourceNotFoundException;

import com.library.book_service.mapper.BookMapper;

import com.library.book_service.repository.BookRepository;

import com.library.book_service.service.BookService;

import feign.FeignException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl
        implements BookService {

    private final BookRepository bookRepository;

    private final CategoryClient categoryClient;

    private final BookMapper bookMapper;

    @Override
    public BookResponseDTO createBook(
            BookRequestDTO request
    ) {
        BookRequestDTO normalizedRequest =
                normalizeRequest(request);

        if (
                bookRepository.existsByIsbnIgnoreCase(
                        normalizedRequest.getIsbn()
                )
        ) {
            log.warn(
                    "Intento de crear libro con ISBN duplicado: {}",
                    normalizedRequest.getIsbn()
            );

            throw new BusinessException(
                    "Ya existe un libro registrado con el ISBN indicado"
            );
        }

        CategoryResponseDTO category =
                validateCategory(
                        normalizedRequest.getCategoryId()
                );

        Book book =
                bookMapper.toEntity(
                        normalizedRequest
                );

        Book savedBook =
                bookRepository.save(book);

        log.info(
                "Libro creado correctamente. id={}, isbn={}",
                savedBook.getId(),
                savedBook.getIsbn()
        );

        return bookMapper.toResponseDTO(
                savedBook,
                category.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {

        List<Book> books =
                bookRepository.findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "title"
                        )
                );

        log.info(
                "Consulta de libros. Total encontrado: {}",
                books.size()
        );

        if (books.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, String> categoryNames =
                getCategoryNames();

        return books.stream()
                .map(book ->
                        bookMapper.toResponseDTO(
                                book,
                                categoryNames.getOrDefault(
                                        book.getCategoryId(),
                                        "Categoría no disponible"
                                )
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(
            Long id
    ) {
        Book book =
                findExistingBook(id);

        CategoryResponseDTO category =
                validateCategory(
                        book.getCategoryId()
                );

        log.info(
                "Libro encontrado. id={}",
                id
        );

        return bookMapper.toResponseDTO(
                book,
                category.getName()
        );
    }

    @Override
    public BookResponseDTO updateBook(
            Long id,
            BookRequestDTO request
    ) {
        Book book =
                findExistingBook(id);

        BookRequestDTO normalizedRequest =
                normalizeRequest(request);

        if (
                bookRepository
                        .existsByIsbnIgnoreCaseAndIdNot(
                                normalizedRequest.getIsbn(),
                                id
                        )
        ) {
            log.warn(
                    "Intento de actualizar libro {} con ISBN duplicado: {}",
                    id,
                    normalizedRequest.getIsbn()
            );

            throw new BusinessException(
                    "Ya existe otro libro registrado con el ISBN indicado"
            );
        }

        CategoryResponseDTO category =
                validateCategory(
                        normalizedRequest.getCategoryId()
                );

        bookMapper.updateEntity(
                book,
                normalizedRequest
        );

        Book updatedBook =
                bookRepository.save(book);

        log.info(
                "Libro actualizado correctamente. id={}",
                id
        );

        return bookMapper.toResponseDTO(
                updatedBook,
                category.getName()
        );
    }

    @Override
    public void deleteBook(
            Long id
    ) {
        Book book =
                findExistingBook(id);

        bookRepository.delete(book);

        log.info(
                "Libro eliminado correctamente. id={}",
                id
        );
    }

    private Book findExistingBook(
            Long id
    ) {
        return bookRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Libro no encontrado. id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "No existe un libro con id " + id
                    );
                });
    }

    private CategoryResponseDTO validateCategory(
            Long categoryId
    ) {
        try {
            return categoryClient.getCategoryById(
                    categoryId
            );

        } catch (FeignException exception) {

            if (exception.status() == 404) {
                log.warn(
                        "Categoría inexistente. id={}",
                        categoryId
                );

                throw new BusinessException(
                        "La categoría indicada no existe"
                );
            }

            if (
                    exception.status() == 401
                            || exception.status() == 403
            ) {
                log.error(
                        "CATEGORY rechazó la validación. status={}",
                        exception.status()
                );

                throw new RemoteServiceException(
                        "CATEGORY rechazó la solicitud de validación"
                );
            }

            log.error(
                    "Error al comunicarse con CATEGORY. status={}",
                    exception.status(),
                    exception
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con CATEGORY"
            );
        }
    }

    private Map<Long, String> getCategoryNames() {

        try {
            List<CategoryResponseDTO> categories =
                    categoryClient.getAllCategories();

            Map<Long, String> result =
                    new HashMap<>();

            for (
                    CategoryResponseDTO category
                    : categories
            ) {
                result.put(
                        category.getId(),
                        category.getName()
                );
            }

            return result;

        } catch (FeignException exception) {

            log.error(
                    "No fue posible obtener las categorías. status={}",
                    exception.status(),
                    exception
            );

            throw new RemoteServiceException(
                    "No fue posible obtener las categorías desde CATEGORY"
            );
        }
    }

    private BookRequestDTO normalizeRequest(
            BookRequestDTO request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Los datos del libro son obligatorios"
            );
        }

        String isbn =
                normalizeAndValidateIsbn(
                        request.getIsbn()
                );

        String title =
                normalizeText(
                        request.getTitle(),
                        "El título es obligatorio"
                );

        String author =
                normalizeText(
                        request.getAuthor(),
                        "El autor es obligatorio"
                );

        validatePublicationYear(
                request.getPublicationYear()
        );

        if (
                request.getCategoryId() == null
                        || request.getCategoryId() <= 0
        ) {
            throw new BusinessException(
                    "La categoría debe ser válida"
            );
        }

        return BookRequestDTO.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .publicationYear(
                        request.getPublicationYear()
                )
                .categoryId(
                        request.getCategoryId()
                )
                .build();
    }

    private String normalizeAndValidateIsbn(
            String isbn
    ) {
        if (
                isbn == null
                        || isbn.isBlank()
        ) {
            throw new BusinessException(
                    "El ISBN es obligatorio"
            );
        }

        String normalizedIsbn =
                isbn.replaceAll(
                                "[\\s-]",
                                ""
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        boolean validIsbn10 =
                normalizedIsbn.matches(
                        "\\d{9}[\\dX]"
                );

        boolean validIsbn13 =
                normalizedIsbn.matches(
                        "\\d{13}"
                );

        if (
                !validIsbn10
                        && !validIsbn13
        ) {
            throw new BusinessException(
                    "El ISBN debe contener 10 o 13 caracteres válidos"
            );
        }

        return normalizedIsbn;
    }

    private String normalizeText(
            String value,
            String errorMessage
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new BusinessException(
                    errorMessage
            );
        }

        return value
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private void validatePublicationYear(
            Integer publicationYear
    ) {
        int currentYear =
                Year.now().getValue();

        if (
                publicationYear == null
                        || publicationYear < 1000
                        || publicationYear > currentYear
        ) {
            throw new BusinessException(
                    "El año de publicación debe estar entre 1000 y "
                            + currentYear
            );
        }
    }
}