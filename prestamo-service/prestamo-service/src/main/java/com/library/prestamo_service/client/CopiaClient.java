package com.library.prestamo_service.client;

import com.library.prestamo_service.client.dto.CopiaRemotaDTO;
import com.library.prestamo_service.client.dto.EstadoCopiaRemota;
import com.library.prestamo_service.config.FeignClientConfig;
import com.library.prestamo_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign para comunicarse con copia-service.
 *
 * El valor "copia-service" debe coincidir con:
 * spring.application.name=copia-service
 */
@FeignClient(
        name = "copia-service",
        path = "/copias",
        configuration = FeignClientConfig.class
)
public interface CopiaClient {

    /**
     * Obtiene una copia por su identificador.
     *
     * Consume:
     * GET /copias/{id}
     */
    @GetMapping("/{id}")
    CopiaRemotaDTO obtenerCopiaPorId(
            @PathVariable("id") Long copiaId
    );

    /**
     * Cambia el estado de una copia.
     *
     * Consume:
     * PATCH /copias/{id}/estado?estado=LOANED
     */
    @PatchMapping("/{id}/estado")
    CopiaRemotaDTO cambiarEstado(
            @PathVariable("id") Long copiaId,
            @RequestParam("estado") EstadoCopiaRemota estado
    );
}