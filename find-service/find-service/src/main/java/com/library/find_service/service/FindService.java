package com.library.find_service.service;

import com.library.find_service.dto.BookDTO;

import java.util.List;

public interface FindService {

    List<BookDTO> getAllBooks();

    List<BookDTO> findByTitle(
            String title
    );

    List<BookDTO> findByAuthor(
            String author
    );

    List<BookDTO> findByCategory(
            String category
    );
}