package com.library.find_service.client;

import com.library.find_service.config.FeignAuthConfig;
import com.library.find_service.dto.BookDTO;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "book-service",
        configuration = FeignAuthConfig.class
)
public interface BookClient {

    @GetMapping("/books")
    List<BookDTO> getAllBooks();
}