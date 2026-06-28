package com.library.book_service.controller;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;
import com.library.book_service.service.BookService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    void createBookShouldReturnCreatedResponse() {

        // Given
        BookRequestDTO request =
                validRequest();

        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookService.createBook(request)
        ).thenReturn(
                expectedResponse
        );

        // When
        ResponseEntity<BookResponseDTO> response =
                bookController.createBook(request);

        // Then
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertSame(
                expectedResponse,
                response.getBody()
        );

        verify(
                bookService
        ).createBook(request);
    }

    @Test
    void getAllBooksShouldReturnOkResponse() {

        // Given
        List<BookResponseDTO> expectedBooks =
                List.of(
                        validResponse()
                );

        when(
                bookService.getAllBooks()
        ).thenReturn(
                expectedBooks
        );

        // When
        ResponseEntity<List<BookResponseDTO>> response =
                bookController.getAllBooks();

        // Then
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                expectedBooks,
                response.getBody()
        );

        verify(
                bookService
        ).getAllBooks();
    }

    @Test
    void getBookByIdShouldReturnOkResponse() {

        // Given
        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookService.getBookById(1L)
        ).thenReturn(
                expectedResponse
        );

        // When
        ResponseEntity<BookResponseDTO> response =
                bookController.getBookById(1L);

        // Then
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                expectedResponse,
                response.getBody()
        );

        verify(
                bookService
        ).getBookById(1L);
    }

    @Test
    void updateBookShouldReturnOkResponse() {

        // Given
        BookRequestDTO request =
                validRequest();

        BookResponseDTO expectedResponse =
                validResponse();

        when(
                bookService.updateBook(
                        1L,
                        request
                )
        ).thenReturn(
                expectedResponse
        );

        // When
        ResponseEntity<BookResponseDTO> response =
                bookController.updateBook(
                        1L,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                expectedResponse,
                response.getBody()
        );

        verify(
                bookService
        ).updateBook(
                1L,
                request
        );
    }

    @Test
    void deleteBookShouldReturnNoContentResponse() {

        // Given
        Long bookId =
                1L;

        // When
        ResponseEntity<Void> response =
                bookController.deleteBook(
                        bookId
                );

        // Then
        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );

        verify(
                bookService
        ).deleteBook(
                bookId
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


