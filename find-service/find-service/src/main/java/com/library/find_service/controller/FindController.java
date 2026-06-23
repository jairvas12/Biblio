package com.library.find_service.controller;

import com.library.find_service.dto.BookDTO;
import com.library.find_service.service.FindService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FindController {

    private final FindService findService;

    public FindController(FindService findService) {
        this.findService = findService;
    }

    @GetMapping("/find/books")
    public List<BookDTO> getBooks() {
        return findService.getAllBooks();
    }

    @GetMapping("/find/title")
    public List<BookDTO> findByTitle(@RequestParam String title) {
        return findService.findByTitle(title);
    }

    @GetMapping("/find/author")
    public List<BookDTO> findByAuthor(@RequestParam String author) {
        return findService.findByAuthor(author);
    }

    @GetMapping("/find/category")
    public List<BookDTO> findByCategory(@RequestParam String category) {
        return findService.findByCategory(category);
    }
}