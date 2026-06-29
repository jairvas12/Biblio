package com.library.security_service.service;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.model.SecurityEventType;

import java.util.List;

public interface SecurityEventService {

    SecurityEventResponseDTO create(
            SecurityEventRequestDTO request
    );

    List<SecurityEventResponseDTO> findAll();

    SecurityEventResponseDTO findById(
            Long id
    );

    List<SecurityEventResponseDTO> findByUserId(
            Long userId
    );

    List<SecurityEventResponseDTO> findByType(
            SecurityEventType type
    );

    void delete(
            Long id
    );
}