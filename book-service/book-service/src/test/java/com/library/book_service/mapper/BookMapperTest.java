package com.library.book_service.mapper;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;
import com.library.book_service.entity.Book;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookMapperTest {

    private final BookMapper bookMapper = new BookMapper();

    @Test
    void toEntityShouldConvertRequestToBook() {

        // Given
        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("9789560000011")
                .title("Historia de Chile")
                .author("Francisco Encina")
                .publicationYear(2020)
                .categoryId(1L)
                .build();

        // When
        Book result = bookMapper.toEntity(request);

        // Then
        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals(
                        "9789560000011",
                        result.getIsbn()
                ),
                () -> assertEquals(
                        "Historia de Chile",
                        result.getTitle()
                ),
                () -> assertEquals(
                        "Francisco Encina",
                        result.getAuthor()
                ),
                () -> assertEquals(
                        2020,
                        result.getPublicationYear()
                ),
                () -> assertEquals(
                        1L,
                        result.getCategoryId()
                )
        );
    }

    @Test
    void updateEntityShouldReplaceBookData() {

        // Given
        Book book = Book.builder()
                .id(1L)
                .isbn("0306406152")
                .title("Título anterior")
                .author("Autor anterior")
                .publicationYear(1990)
                .categoryId(2L)
                .build();

        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("9789560000011")
                .title("Título actualizado")
                .author("Autor actualizado")
                .publicationYear(2024)
                .categoryId(3L)
                .build();

        // When
        bookMapper.updateEntity(
                book,
                request
        );

        // Then
        assertAll(
                () -> assertEquals(
                        1L,
                        book.getId()
                ),
                () -> assertEquals(
                        "9789560000011",
                        book.getIsbn()
                ),
                () -> assertEquals(
                        "Título actualizado",
                        book.getTitle()
                ),
                () -> assertEquals(
                        "Autor actualizado",
                        book.getAuthor()
                ),
                () -> assertEquals(
                        2024,
                        book.getPublicationYear()
                ),
                () -> assertEquals(
                        3L,
                        book.getCategoryId()
                )
        );
    }

    @Test
    void toResponseDTOShouldIncludeCategoryName() {

        // Given
        Book book = Book.builder()
                .id(1L)
                .isbn("9789560000011")
                .title("Historia de Chile")
                .author("Francisco Encina")
                .publicationYear(2020)
                .categoryId(1L)
                .build();

        // When
        BookResponseDTO result = bookMapper.toResponseDTO(
                book,
                "Historia"
        );

        // Then
        assertAll(
                () -> assertEquals(
                        1L,
                        result.getId()
                ),
                () -> assertEquals(
                        "9789560000011",
                        result.getIsbn()
                ),
                () -> assertEquals(
                        "Historia de Chile",
                        result.getTitle()
                ),
                () -> assertEquals(
                        "Francisco Encina",
                        result.getAuthor()
                ),
                () -> assertEquals(
                        2020,
                        result.getPublicationYear()
                ),
                () -> assertEquals(
                        1L,
                        result.getCategoryId()
                ),
                () -> assertEquals(
                        "Historia",
                        result.getCategory()
                )
        );
    }
}

