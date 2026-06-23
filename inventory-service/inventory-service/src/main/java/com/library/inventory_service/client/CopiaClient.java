package com.library.inventory_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "copia-service")
public interface CopiaClient {

    @GetMapping("/copias/libro/{bookId}/disponibles/cantidad")
    Long contarCopiasDisponiblesPorLibro(@PathVariable("bookId") Long bookId);
}