package com.library.inventory_service.client;

import com.library.inventory_service.dto.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/books/{id}")
    BookResponseDTO buscarLibroPorId(@PathVariable("id") Long id);
}