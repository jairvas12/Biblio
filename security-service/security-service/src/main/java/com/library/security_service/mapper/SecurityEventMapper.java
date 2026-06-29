package com.library.security_service.mapper;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.model.SecurityEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventMapper {

    public SecurityEvent toEntity(
            SecurityEventRequestDTO request
    ) {

        if (request == null) {
            return null;
        }

        return SecurityEvent.builder()
                .userId(request.userId())
                .username(request.username())
                .type(request.type())
                .description(request.description())
                .ipAddress(request.ipAddress())
                .successful(request.successful())
                .build();
    }

    public SecurityEventResponseDTO toResponse(
            SecurityEvent event
    ) {

        if (event == null) {
            return null;
        }

        return new SecurityEventResponseDTO(
                event.getId(),
                event.getUserId(),
                event.getUsername(),
                event.getType(),
                event.getDescription(),
                event.getIpAddress(),
                event.isSuccessful(),
                event.getCreatedAt()
        );
    }
}