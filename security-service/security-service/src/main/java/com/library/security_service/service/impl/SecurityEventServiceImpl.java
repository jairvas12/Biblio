package com.library.security_service.service.impl;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.exception.BusinessException;
import com.library.security_service.exception.ResourceNotFoundException;
import com.library.security_service.mapper.SecurityEventMapper;
import com.library.security_service.model.SecurityEvent;
import com.library.security_service.model.SecurityEventType;
import com.library.security_service.repository.SecurityEventRepository;
import com.library.security_service.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SecurityEventServiceImpl
        implements SecurityEventService {

    private final SecurityEventRepository repository;
    private final SecurityEventMapper mapper;

    @Override
    public SecurityEventResponseDTO create(
            SecurityEventRequestDTO request
    ) {

        if (request == null) {
            throw new BusinessException(
                    "La solicitud del evento de seguridad no puede ser nula"
            );
        }

        if (request.successful() == null) {
            throw new BusinessException(
                    "El resultado del evento de seguridad es obligatorio"
            );
        }

        SecurityEvent event = mapper.toEntity(request);

        SecurityEvent savedEvent =
                repository.save(event);

        log.info(
                "Evento de seguridad creado con ID {}, tipo {} y usuario {}",
                savedEvent.getId(),
                savedEvent.getType(),
                savedEvent.getUserId()
        );

        return mapper.toResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityEventResponseDTO> findAll() {

        return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityEventResponseDTO findById(
            Long id
    ) {

        return mapper.toResponse(
                findEntityById(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityEventResponseDTO> findByUserId(
            Long userId
    ) {

        validatePositiveId(
                userId,
                "El ID del usuario debe ser mayor que cero"
        );

        return repository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityEventResponseDTO> findByType(
            SecurityEventType type
    ) {

        if (type == null) {
            throw new BusinessException(
                    "El tipo de evento de seguridad es obligatorio"
            );
        }

        return repository
                .findByTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(
            Long id
    ) {

        SecurityEvent event = findEntityById(id);

        repository.delete(event);

        log.info(
                "Evento de seguridad {} eliminado",
                id
        );
    }

    private SecurityEvent findEntityById(
            Long id
    ) {

        validatePositiveId(
                id,
                "El ID del evento de seguridad debe ser mayor que cero"
        );

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el evento de seguridad con ID "
                                        + id
                        )
                );
    }

    private void validatePositiveId(
            Long id,
            String message
    ) {

        if (id == null || id <= 0) {
            throw new BusinessException(message);
        }
    }
}