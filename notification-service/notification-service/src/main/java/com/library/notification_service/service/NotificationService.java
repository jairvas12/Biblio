package com.library.notification_service.service;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO create(
            NotificationRequestDTO request
    );

    NotificationResponseDTO findById(
            Long id
    );

    List<NotificationResponseDTO> findByUserId(
            Long userId
    );

    List<NotificationResponseDTO> findUnreadByUserId(
            Long userId
    );

    long countUnreadByUserId(
            Long userId
    );

    NotificationResponseDTO markAsRead(
            Long id
    );

    void delete(
            Long id
    );
}