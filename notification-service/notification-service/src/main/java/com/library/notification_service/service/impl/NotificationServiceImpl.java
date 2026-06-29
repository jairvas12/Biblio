package com.library.notification_service.service.impl;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.exception.BusinessException;
import com.library.notification_service.exception.ResourceNotFoundException;
import com.library.notification_service.mapper.NotificationMapper;
import com.library.notification_service.model.Notification;
import com.library.notification_service.repository.NotificationRepository;
import com.library.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    public NotificationResponseDTO create(
            NotificationRequestDTO request
    ) {

        if (request == null) {
            throw new BusinessException(
                    "La solicitud de notificación no puede ser nula"
            );
        }

        Notification notification = mapper.toEntity(request);
        Notification savedNotification =
                repository.save(notification);

        log.info(
                "Notificación creada con ID {} para el usuario {}",
                savedNotification.getId(),
                savedNotification.getUserId()
        );

        return mapper.toResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO findById(
            Long id
    ) {

        Notification notification = findEntityById(id);

        return mapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findByUserId(
            Long userId
    ) {

        return repository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findUnreadByUserId(
            Long userId
    ) {

        return repository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(
            Long userId
    ) {

        return repository.countByUserIdAndReadFalse(
                userId
        );
    }

    @Override
    public NotificationResponseDTO markAsRead(
            Long id
    ) {

        Notification notification = findEntityById(id);

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = repository.save(notification);

            log.info(
                    "Notificación {} marcada como leída",
                    id
            );
        }

        return mapper.toResponse(notification);
    }

    @Override
    public void delete(
            Long id
    ) {

        Notification notification = findEntityById(id);

        repository.delete(notification);

        log.info(
                "Notificación {} eliminada",
                id
        );
    }

    private Notification findEntityById(
            Long id
    ) {

        if (id == null || id <= 0) {
            throw new BusinessException(
                    "El ID de la notificación debe ser mayor que cero"
            );
        }

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró la notificación con ID "
                                        + id
                        )
                );
    }
}