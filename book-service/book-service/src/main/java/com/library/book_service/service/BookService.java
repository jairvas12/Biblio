package com.library.book_service.service;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;

import java.util.List;

public interface BookService {

    BookResponseDTO createBook(
            BookRequestDTO request
    );

    List<BookResponseDTO> getAllBooks();

    BookResponseDTO getBookById(
            Long id
    );

    BookResponseDTO updateBook(
            Long id,
            BookRequestDTO request
    );

    void deleteBook(
            Long id
    );
}