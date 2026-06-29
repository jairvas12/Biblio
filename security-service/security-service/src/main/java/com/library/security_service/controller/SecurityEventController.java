package com.library.security_service.controller;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.model.SecurityEventType;
import com.library.security_service.service.SecurityEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/security/events")
@RequiredArgsConstructor
public class SecurityEventController {

    private final SecurityEventService service;

    @PostMapping
    public ResponseEntity<SecurityEventResponseDTO> create(
            @Valid
            @RequestBody
            SecurityEventRequestDTO request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SecurityEventResponseDTO>> findAll() {

        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecurityEventResponseDTO> findById(

            @PathVariable
            @Positive(
                    message = "El ID del evento de seguridad debe ser mayor que cero"
            )
            Long id
    ) {

        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SecurityEventResponseDTO>> findByUserId(

            @PathVariable
            @Positive(
                    message = "El ID del usuario debe ser mayor que cero"
            )
            Long userId
    ) {

        return ResponseEntity.ok(
                service.findByUserId(userId)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SecurityEventResponseDTO>> findByType(

            @PathVariable
            SecurityEventType type
    ) {

        return ResponseEntity.ok(
                service.findByType(type)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(

            @PathVariable
            @Positive(
                    message = "El ID del evento de seguridad debe ser mayor que cero"
            )
            Long id
    ) {

        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}