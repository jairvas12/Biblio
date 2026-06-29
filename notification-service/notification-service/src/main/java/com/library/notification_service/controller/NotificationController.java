package com.library.notification_service.controller;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid
            @RequestBody
            NotificationRequestDTO request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> findById(

            @PathVariable
            @Positive(
                    message = "El ID de la notificación debe ser mayor que cero"
            )
            Long id
    ) {

        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> findByUserId(

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

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDTO>>
    findUnreadByUserId(

            @PathVariable
            @Positive(
                    message = "El ID del usuario debe ser mayor que cero"
            )
            Long userId
    ) {

        return ResponseEntity.ok(
                service.findUnreadByUserId(userId)
        );
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Long>> countUnreadByUserId(

            @PathVariable
            @Positive(
                    message = "El ID del usuario debe ser mayor que cero"
            )
            Long userId
    ) {

        long count = service.countUnreadByUserId(userId);

        return ResponseEntity.ok(
                Map.of("unreadCount", count)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(

            @PathVariable
            @Positive(
                    message = "El ID de la notificación debe ser mayor que cero"
            )
            Long id
    ) {

        return ResponseEntity.ok(
                service.markAsRead(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(

            @PathVariable
            @Positive(
                    message = "El ID de la notificación debe ser mayor que cero"
            )
            Long id
    ) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}