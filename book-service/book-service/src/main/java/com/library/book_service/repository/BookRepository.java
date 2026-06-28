package com.library.book_service.repository;

import com.library.book_service.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository
        extends JpaRepository<Book, Long> {

    boolean existsByIsbnIgnoreCase(
            String isbn
    );

    boolean existsByIsbnIgnoreCaseAndIdNot(
            String isbn,
            Long id
    );
}