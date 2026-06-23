package com.library.copia_service.client;

import com.library.copia_service.dto.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/books/{id}")
    BookResponseDTO buscarLibroPorId(@PathVariable("id") Long id);
}