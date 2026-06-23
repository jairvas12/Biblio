package com.library.book_service.service;

import com.library.book_service.entity.Book;

import java.util.List;

public interface BookService {

    Book saveBook(Book book);

    List<Book> getAllBooks();

    Book getBookById(Long id);

    Book updateBook(Long id, Book updatedBook);

    void deleteBook(Long id);
}