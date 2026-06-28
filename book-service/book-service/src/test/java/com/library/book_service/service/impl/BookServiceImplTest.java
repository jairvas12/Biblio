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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryClient categoryClient;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void createBookShouldNormalizeAndSaveBook() {

        // Given
        BookRequestDTO request = validRequest();

        request.setIsbn("978-956-0000-01-1");
        request.setTitle("  Historia   de Chile  ");
        request.setAuthor("  Francisco   Encina ");

        Book mappedBook = validBook();
        mappedBook.setId(null);

        Book savedBook = validBook();

        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenReturn(
                validCategory()
        );

        when(
                bookMapper.toEntity(
                        any(BookRequestDTO.class)
                )
        ).thenReturn(mappedBook);

        when(
                bookRepository.save(mappedBook)
        ).thenReturn(savedBook);

        when(
                bookMapper.toResponseDTO(
                        savedBook,
                        "Historia"
                )
        ).thenReturn(expectedResponse);

        // When
        BookResponseDTO result =
                bookService.createBook(request);

        // Then
        assertSame(
                expectedResponse,
                result
        );

        ArgumentCaptor<BookRequestDTO> captor =
                ArgumentCaptor.forClass(
                        BookRequestDTO.class
                );

        verify(bookMapper).toEntity(
                captor.capture()
        );

        BookRequestDTO normalizedRequest =
                captor.getValue();

        assertEquals(
                "9789560000011",
                normalizedRequest.getIsbn()
        );

        assertEquals(
                "Historia de Chile",
                normalizedRequest.getTitle()
        );

        assertEquals(
                "Francisco Encina",
                normalizedRequest.getAuthor()
        );

        assertEquals(
                2020,
                normalizedRequest.getPublicationYear()
        );

        assertEquals(
                1L,
                normalizedRequest.getCategoryId()
        );

        verify(
                bookRepository
        ).save(mappedBook);

        verify(
                categoryClient
        ).getCategoryById(1L);
    }

    @Test
    void createBookShouldRejectDuplicatedIsbn() {

        // Given
        BookRequestDTO request =
                validRequest();

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(true);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "Ya existe un libro registrado con el ISBN indicado",
                exception.getMessage()
        );

        verifyNoInteractions(
                categoryClient
        );

        verify(
                bookMapper,
                never()
        ).toEntity(any());

        verify(
                bookRepository,
                never()
        ).save(any());
    }

    @Test
    void createBookShouldRejectNullRequest() {

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                null
                        )
                );

        // Then
        assertEquals(
                "Los datos del libro son obligatorios",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }
    @Test
    void createBookShouldRejectBlankIsbn() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setIsbn(" ");

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El ISBN es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectInvalidIsbn() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setIsbn("12345");

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El ISBN debe contener 10 o 13 caracteres válidos",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectBlankTitle() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setTitle(" ");

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El título es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectBlankAuthor() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setAuthor(" ");

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El autor es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectNullPublicationYear() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setPublicationYear(null);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El año de publicación debe estar entre 1000 y "
                        + java.time.Year.now().getValue(),
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectPublicationYearBefore1000() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setPublicationYear(999);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El año de publicación debe estar entre 1000 y "
                        + java.time.Year.now().getValue(),
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectFuturePublicationYear() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setPublicationYear(
                java.time.Year.now().getValue() + 1
        );

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "El año de publicación debe estar entre 1000 y "
                        + java.time.Year.now().getValue(),
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectNullCategoryId() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setCategoryId(null);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "La categoría debe ser válida",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectNonPositiveCategoryId() {

        // Given
        BookRequestDTO request =
                validRequest();

        request.setCategoryId(0L);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "La categoría debe ser válida",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookRepository,
                categoryClient,
                bookMapper
        );
    }

    @Test
    void createBookShouldRejectMissingCategory() {

        // Given
        BookRequestDTO request =
                validRequest();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(404);

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "La categoría indicada no existe",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).toEntity(any());

        verify(
                bookRepository,
                never()
        ).save(any());
    }

    @Test
    void createBookShouldHandleCategoryUnauthorizedResponse() {

        // Given
        BookRequestDTO request =
                validRequest();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(401);

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "CATEGORY rechazó la solicitud de validación",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).toEntity(any());

        verify(
                bookRepository,
                never()
        ).save(any());
    }

    @Test
    void createBookShouldHandleCategoryForbiddenResponse() {

        // Given
        BookRequestDTO request =
                validRequest();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(403);

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "CATEGORY rechazó la solicitud de validación",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).toEntity(any());

        verify(
                bookRepository,
                never()
        ).save(any());
    }


    @Test
    void createBookShouldHandleRemoteCategoryFailure() {

        // Given
        BookRequestDTO request =
                validRequest();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(500);

        when(
                bookRepository.existsByIsbnIgnoreCase(
                        "9789560000011"
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.createBook(
                                request
                        )
                );

        // Then
        assertEquals(
                "No fue posible comunicarse con CATEGORY",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).toEntity(any());

        verify(
                bookRepository,
                never()
        ).save(any());
    }

    @Test
    void getAllBooksShouldReturnEmptyList() {

        // Given
        when(
                bookRepository.findAll(
                        any(org.springframework.data.domain.Sort.class)
                )
        ).thenReturn(
                java.util.List.of()
        );

        // When
        java.util.List<BookResponseDTO> result =
                bookService.getAllBooks();

        // Then
        assertTrue(
                result.isEmpty()
        );

        verifyNoInteractions(
                categoryClient
        );

        verify(
                bookMapper,
                never()
        ).toResponseDTO(
                any(),
                any()
        );
    }

    @Test
    void getAllBooksShouldReturnBooksAndFallbackCategory() {

        // Given
        Book firstBook =
                validBook();

        Book secondBook =
                Book.builder()
                        .id(2L)
                        .isbn("0306406152")
                        .title("Otro libro")
                        .author("Otro autor")
                        .publicationYear(2010)
                        .categoryId(99L)
                        .build();

        BookResponseDTO firstResponse =
                validResponse();

        BookResponseDTO secondResponse =
                BookResponseDTO.builder()
                        .id(2L)
                        .isbn("0306406152")
                        .title("Otro libro")
                        .author("Otro autor")
                        .publicationYear(2010)
                        .categoryId(99L)
                        .category("Categoría no disponible")
                        .build();

        when(
                bookRepository.findAll(
                        any(org.springframework.data.domain.Sort.class)
                )
        ).thenReturn(
                java.util.List.of(
                        firstBook,
                        secondBook
                )
        );

        when(
                categoryClient.getAllCategories()
        ).thenReturn(
                java.util.List.of(
                        validCategory()
                )
        );

        when(
                bookMapper.toResponseDTO(
                        firstBook,
                        "Historia"
                )
        ).thenReturn(
                firstResponse
        );

        when(
                bookMapper.toResponseDTO(
                        secondBook,
                        "Categoría no disponible"
                )
        ).thenReturn(
                secondResponse
        );

        // When
        java.util.List<BookResponseDTO> result =
                bookService.getAllBooks();

        // Then
        assertEquals(
                2,
                result.size()
        );

        assertSame(
                firstResponse,
                result.get(0)
        );

        assertSame(
                secondResponse,
                result.get(1)
        );

        verify(
                categoryClient
        ).getAllCategories();

        verify(
                bookMapper
        ).toResponseDTO(
                firstBook,
                "Historia"
        );

        verify(
                bookMapper
        ).toResponseDTO(
                secondBook,
                "Categoría no disponible"
        );
    }

    @Test
    void getAllBooksShouldHandleRemoteFailure() {

        // Given
        when(
                bookRepository.findAll(
                        any(org.springframework.data.domain.Sort.class)
                )
        ).thenReturn(
                java.util.List.of(
                        validBook()
                )
        );

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(500);

        when(
                categoryClient.getAllCategories()
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.getAllBooks()
                );

        // Then
        assertEquals(
                "No fue posible obtener las categorías desde CATEGORY",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getAllCategories();

        verify(
                bookMapper,
                never()
        ).toResponseDTO(
                any(),
                any()
        );
    }

    @Test
    void getBookByIdShouldReturnBook() {

        // Given
        Book book =
                validBook();

        CategoryResponseDTO category =
                validCategory();

        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(book)
        );

        when(
                categoryClient.getCategoryById(1L)
        ).thenReturn(
                category
        );

        when(
                bookMapper.toResponseDTO(
                        book,
                        "Historia"
                )
        ).thenReturn(
                expectedResponse
        );

        // When
        BookResponseDTO result =
                bookService.getBookById(1L);

        // Then
        assertSame(
                expectedResponse,
                result
        );

        verify(
                bookRepository
        ).findById(1L);

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper
        ).toResponseDTO(
                book,
                "Historia"
        );
    }

    @Test
    void getBookByIdShouldThrowWhenBookDoesNotExist() {

        // Given
        when(
                bookRepository.findById(999L)
        ).thenReturn(
                java.util.Optional.empty()
        );

        // When
        assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBookById(999L)
        );

        // Then
        verify(
                bookRepository
        ).findById(999L);

        verifyNoInteractions(
                categoryClient,
                bookMapper
        );
    }

    @Test
    void deleteBookShouldDeleteExistingBook() {

        // Given
        Book book =
                validBook();

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(book)
        );

        // When
        bookService.deleteBook(1L);

        // Then
        verify(
                bookRepository
        ).findById(1L);

        verify(
                bookRepository
        ).delete(book);

        verifyNoInteractions(
                categoryClient,
                bookMapper
        );
    }

    @Test
    void deleteBookShouldThrowWhenBookDoesNotExist() {

        // Given
        when(
                bookRepository.findById(999L)
        ).thenReturn(
                java.util.Optional.empty()
        );

        // When
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> bookService.deleteBook(999L)
                );

        // Then
        assertEquals(
                "No existe un libro con id 999",
                exception.getMessage()
        );

        verify(
                bookRepository
        ).findById(999L);

        verify(
                bookRepository,
                never()
        ).delete(any());

        verifyNoInteractions(
                categoryClient,
                bookMapper
        );
    }

    @Test
    void updateBookShouldUpdateExistingBook() {

        // Given
        BookRequestDTO request =
                validRequest();

        Book existingBook =
                validBook();

        CategoryResponseDTO category =
                validCategory();

        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(existingBook)
        );

        when(
                bookRepository.existsByIsbnIgnoreCaseAndIdNot(
                        "9789560000011",
                        1L
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenReturn(category);

        when(
                bookRepository.save(existingBook)
        ).thenReturn(existingBook);

        when(
                bookMapper.toResponseDTO(
                        existingBook,
                        "Historia"
                )
        ).thenReturn(expectedResponse);

        // When
        BookResponseDTO result =
                bookService.updateBook(
                        1L,
                        request
                );

        // Then
        assertSame(
                expectedResponse,
                result
        );

        verify(
                bookRepository
        ).findById(1L);

        verify(
                bookRepository
        ).existsByIsbnIgnoreCaseAndIdNot(
                "9789560000011",
                1L
        );

        verify(
                categoryClient
        ).getCategoryById(1L);


        ArgumentCaptor<BookRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(
                        BookRequestDTO.class
                );

        verify(
                bookMapper
        ).updateEntity(
                org.mockito.ArgumentMatchers.same(existingBook),
                requestCaptor.capture()
        );

        BookRequestDTO normalizedRequest =
                requestCaptor.getValue();

        assertEquals(
                request.getIsbn(),
                normalizedRequest.getIsbn()
        );

        assertEquals(
                request.getTitle(),
                normalizedRequest.getTitle()
        );

        assertEquals(
                request.getAuthor(),
                normalizedRequest.getAuthor()
        );

        assertEquals(
                request.getPublicationYear(),
                normalizedRequest.getPublicationYear()
        );

        assertEquals(
                request.getCategoryId(),
                normalizedRequest.getCategoryId()
        );

        verify(
                bookRepository
        ).save(existingBook);

        verify(
                bookMapper
        ).toResponseDTO(
                existingBook,
                "Historia"
        );
    }

    @Test
    void updateBookShouldThrowWhenBookDoesNotExist() {

        // Given
        BookRequestDTO request =
                validRequest();

        when(
                bookRepository.findById(999L)
        ).thenReturn(
                java.util.Optional.empty()
        );

        // When
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> bookService.updateBook(
                                999L,
                                request
                        )
                );

        // Then
        assertEquals(
                "No existe un libro con id 999",
                exception.getMessage()
        );

        verify(
                bookRepository
        ).findById(999L);

        verify(
                bookRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                categoryClient,
                bookMapper
        );
    }

    @Test
    void updateBookShouldRejectDuplicatedIsbn() {

        // Given
        BookRequestDTO request =
                validRequest();

        Book existingBook =
                validBook();

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(existingBook)
        );

        when(
                bookRepository.existsByIsbnIgnoreCaseAndIdNot(
                        "9789560000011",
                        1L
                )
        ).thenReturn(true);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.updateBook(
                                1L,
                                request
                        )
                );

        // Then
        verify(
                bookRepository
        ).findById(1L);

        verify(
                bookRepository
        ).existsByIsbnIgnoreCaseAndIdNot(
                "9789560000011",
                1L
        );

        verify(
                bookRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                categoryClient,
                bookMapper
        );
    }

    @Test
    void updateBookShouldRejectMissingCategory() {

        // Given
        BookRequestDTO request =
                validRequest();

        Book existingBook =
                validBook();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(404);

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(existingBook)
        );

        when(
                bookRepository.existsByIsbnIgnoreCaseAndIdNot(
                        "9789560000011",
                        1L
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.updateBook(
                                1L,
                                request
                        )
                );

        // Then
        assertEquals(
                "La categoría indicada no existe",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).updateEntity(
                any(),
                any()
        );

        verify(
                bookRepository,
                never()
        ).save(any());
    }

    @Test
    void updateBookShouldHandleRemoteCategoryFailure() {

        // Given
        BookRequestDTO request =
                validRequest();

        Book existingBook =
                validBook();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(500);

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(existingBook)
        );

        when(
                bookRepository.existsByIsbnIgnoreCaseAndIdNot(
                        "9789560000011",
                        1L
                )
        ).thenReturn(false);

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.updateBook(
                                1L,
                                request
                        )
                );

        // Then
        assertEquals(
                "No fue posible comunicarse con CATEGORY",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verify(
                bookMapper,
                never()
        ).updateEntity(
                any(),
                any()
        );

        verify(
                bookRepository,
                never()
        ).save(any());
    }    


    @Test
    void getBookByIdShouldRejectMissingCategory() {

        // Given
        Book book =
                validBook();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(404);

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(book)
        );

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> bookService.getBookById(1L)
                );

        // Then
        assertEquals(
                "La categoría indicada no existe",
                exception.getMessage()
        );

        verify(
                bookRepository
        ).findById(1L);

        verify(
                categoryClient
        ).getCategoryById(1L);

        verifyNoInteractions(
                bookMapper
        );
    }

    @Test
    void getBookByIdShouldHandleCategoryUnauthorizedResponse() {

        // Given
        Book book =
                validBook();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(401);

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(book)
        );

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.getBookById(1L)
                );

        // Then
        assertEquals(
                "CATEGORY rechazó la solicitud de validación",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verifyNoInteractions(
                bookMapper
        );
    }

    @Test
    void getBookByIdShouldHandleRemoteCategoryFailure() {

        // Given
        Book book =
                validBook();

        feign.FeignException feignException =
                org.mockito.Mockito.mock(
                        feign.FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(500);

        when(
                bookRepository.findById(1L)
        ).thenReturn(
                java.util.Optional.of(book)
        );

        when(
                categoryClient.getCategoryById(1L)
        ).thenThrow(feignException);

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () -> bookService.getBookById(1L)
                );

        // Then
        assertEquals(
                "No fue posible comunicarse con CATEGORY",
                exception.getMessage()
        );

        verify(
                categoryClient
        ).getCategoryById(1L);

        verifyNoInteractions(
                bookMapper
        );
    }

    private BookRequestDTO validRequest() {

        return BookRequestDTO.builder()
                .isbn("9789560000011")
                .title("Historia de Chile")
                .author("Francisco Encina")
                .publicationYear(2020)
                .categoryId(1L)
                .build();
    }

    private Book validBook() {

        return Book.builder()
                .id(1L)
                .isbn("9789560000011")
                .title("Historia de Chile")
                .author("Francisco Encina")
                .publicationYear(2020)
                .categoryId(1L)
                .build();
    }

    private CategoryResponseDTO validCategory() {

        return new CategoryResponseDTO(
                1L,
                "Historia"
        );
    }

    private BookResponseDTO validResponse() {

        return BookResponseDTO.builder()
                .id(1L)
                .isbn("9789560000011")
                .title("Historia de Chile")
                .author("Francisco Encina")
                .publicationYear(2020)
                .categoryId(1L)
                .category("Historia")
                .build();
    }
}