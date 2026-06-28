package com.library.book_service.mapper;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;
import com.library.book_service.entity.Book;

import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(
            BookRequestDTO request
    ) {
        return Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publicationYear(
                        request.getPublicationYear()
                )
                .categoryId(
                        request.getCategoryId()
                )
                .build();
    }

    public void updateEntity(
            Book book,
            BookRequestDTO request
    ) {
        book.setIsbn(
                request.getIsbn()
        );

        book.setTitle(
                request.getTitle()
        );

        book.setAuthor(
                request.getAuthor()
        );

        book.setPublicationYear(
                request.getPublicationYear()
        );

        book.setCategoryId(
                request.getCategoryId()
        );
    }

    public BookResponseDTO toResponseDTO(
            Book book,
            String categoryName
    ) {
        return BookResponseDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(
                        book.getPublicationYear()
                )
                .categoryId(
                        book.getCategoryId()
                )
                .category(categoryName)
                .build();
    }
}