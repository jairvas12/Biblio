package com.library.copia_service.client;

import com.library.copia_service.config.FeignAuthConfig;
import com.library.copia_service.dto.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "book-service",
        configuration = FeignAuthConfig.class
)
public interface BookClient {

    @GetMapping("/books/{id}")
    BookResponseDTO buscarLibroPorId(
            @PathVariable("id") Long id
    );
}