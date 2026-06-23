package com.library.find_service.service;


import com.library.find_service.client.BookClient;
import com.library.find_service.dto.BookDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindService {

    private final BookClient bookClient;

    public FindService(BookClient bookClient) {
        this.bookClient = bookClient;
    }

    public List<BookDTO> getAllBooks() {
        return bookClient.getAllBooks();
    }

    public List<BookDTO> findByTitle(String title) {

        return bookClient.getAllBooks()
                .stream()
                .filter(book ->
                        book.getTitle()
                                .toLowerCase()
                                .contains(title.toLowerCase()))
                .toList();
    }

    public List<BookDTO> findByAuthor(String author) {

        return bookClient.getAllBooks()
                .stream()
                .filter(book ->
                        book.getAuthor()
                                .toLowerCase()
                                .contains(author.toLowerCase()))
                .toList();
    }

    public List<BookDTO> findByCategory(String category) {

        return bookClient.getAllBooks()
                .stream()
                .filter(book ->
                        book.getCategory()
                                .toLowerCase()
                                .contains(category.toLowerCase()))
                .toList();
    }
}
