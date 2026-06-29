package com.library.find_service.service.impl;

import com.library.find_service.client.BookClient;
import com.library.find_service.dto.BookDTO;
import com.library.find_service.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.library.find_service.exception.RemoteServiceException;

import feign.FeignException;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindServiceImplTest {

    @Mock
    private BookClient bookClient;

    private FindServiceImpl findService;

    @BeforeEach
    void setUp() {

        findService =
                new FindServiceImpl(
                        bookClient
                );
    }

    @Test
    void getAllBooksShouldReturnCatalogFromBookService() {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "9789560000011",
                                "Historia de Chile",
                                "Francisco Encina",
                                2020,
                                1L,
                                "Historia"
                        ),
                        createBook(
                                2L,
                                "9789560000028",
                                "La casa de los espíritus",
                                "Isabel Allende",
                                1982,
                                2L,
                                "Novela"
                        )
                );

        when(
                bookClient.getAllBooks()
        ).thenReturn(
                books
        );

        // When
        List<BookDTO> result =
                findService.getAllBooks();

        // Then
        assertSame(
                books,
                result
        );

        assertEquals(
                2,
                result.size()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void findByTitleShouldIgnoreCaseAndSpaces() {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "9789560000011",
                                "Historia de Chile",
                                "Francisco Encina",
                                2020,
                                1L,
                                "Historia"
                        ),
                        createBook(
                                2L,
                                "9789560000028",
                                "La casa de los espíritus",
                                "Isabel Allende",
                                1982,
                                2L,
                                "Novela"
                        ),
                        createBook(
                                3L,
                                "9789560000035",
                                null,
                                "Autor desconocido",
                                2000,
                                3L,
                                "Ensayo"
                        )
                );

        when(
                bookClient.getAllBooks()
        ).thenReturn(
                books
        );

        // When
        List<BookDTO> result =
                findService.findByTitle(
                        "  HISTORIA  "
                );

        // Then
        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Historia de Chile",
                result.get(0).getTitle()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void findByAuthorShouldReturnPartialMatchesIgnoringCase() {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "9789560000011",
                                "Historia de Chile",
                                "Francisco Encina",
                                2020,
                                1L,
                                "Historia"
                        ),
                        createBook(
                                2L,
                                "9789560000028",
                                "La casa de los espíritus",
                                "Isabel Allende",
                                1982,
                                2L,
                                "Novela"
                        ),
                        createBook(
                                3L,
                                "9789560000035",
                                "Libro sin autor",
                                null,
                                2000,
                                3L,
                                "Ensayo"
                        )
                );

        when(
                bookClient.getAllBooks()
        ).thenReturn(
                books
        );

        // When
        List<BookDTO> result =
                findService.findByAuthor(
                        "allende"
                );

        // Then
        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Isabel Allende",
                result.get(0).getAuthor()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void findByCategoryShouldIgnoreCaseSpacesAndNullValues() {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "9789560000011",
                                "Historia de Chile",
                                "Francisco Encina",
                                2020,
                                1L,
                                "Historia"
                        ),
                        createBook(
                                2L,
                                "9789560000028",
                                "La casa de los espíritus",
                                "Isabel Allende",
                                1982,
                                2L,
                                "Novela"
                        ),
                        createBook(
                                3L,
                                "9789560000035",
                                "Libro sin categoría",
                                "Autor desconocido",
                                2000,
                                null,
                                null
                        )
                );

        when(
                bookClient.getAllBooks()
        ).thenReturn(
                books
        );

        // When
        List<BookDTO> result =
                findService.findByCategory(
                        "  NOVELA  "
                );

        // Then
        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Novela",
                result.get(0).getCategory()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void findByTitleShouldThrowBusinessExceptionWhenTitleIsBlank() {

        // Given
        String blankTitle =
                "   ";

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                findService.findByTitle(
                                        blankTitle
                                )
                );

        // Then
        assertEquals(
                "El parámetro título es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookClient
        );
    }

    @Test
    void getAllBooksShouldReturnEmptyListWhenBookServiceReturnsNull() {

        // Given
        when(
                bookClient.getAllBooks()
        ).thenReturn(
                null
        );

        // When
        List<BookDTO> result =
                findService.getAllBooks();

        // Then
        assertEquals(
                0,
                result.size()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void findByAuthorShouldThrowBusinessExceptionWhenAuthorIsNull() {

        // Given
        String author =
                null;

        // When
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                findService.findByAuthor(
                                        author
                                )
                );

        // Then
        assertEquals(
                "El parámetro autor es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookClient
        );
    }

    @Test
    void getAllBooksShouldThrowRemoteServiceExceptionWhenBookRejectsRequest() {

        // Given
        FeignException feignException =
                mock(
                        FeignException.class
                );

        when(
                feignException.status()
        ).thenReturn(
                401
        );

        when(
                bookClient.getAllBooks()
        ).thenThrow(
                feignException
        );

        // When
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () ->
                                findService.getAllBooks()
                );

        // Then
        assertEquals(
                "BOOK rechazó la solicitud de consulta",
                exception.getMessage()
        );

        verify(
                bookClient
        ).getAllBooks();
    }

    @Test
    void getAllBooksShouldThrowRemoteServiceExceptionWhenBookFails() {

        // Given
        FeignException feignException =
                mock(
                        FeignException.class
                );
        when(
                feignException.status()
        ).thenReturn(
                500
        );
        when(
                bookClient.getAllBooks()
        ).thenThrow(
                feignException
        );
        RemoteServiceException exception =
                assertThrows(
                        RemoteServiceException.class,
                        () ->
                                findService.getAllBooks()
                );
        assertEquals(
                "No fue posible obtener el catálogo desde BOOK",
                exception.getMessage()
        );
        verify(
                bookClient
        ).getAllBooks();
    }
    private BookDTO createBook(
            Long id,
            String isbn,
            String title,
            String author,
            Integer publicationYear,
            Long categoryId,
            String category
    ) {
        return BookDTO.builder()
                .id(id)
                .isbn(isbn)
                .title(title)
                .author(author)
                .publicationYear(
                        publicationYear
                )
                .categoryId(
                        categoryId
                )
                .category(category)
                .build();
    }
}