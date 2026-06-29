package com.library.notification_service.mapper;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequestDTO request) {

        if (request == null) {
            return null;
        }

        return Notification.builder()
                .userId(request.userId())
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .read(false)
                .build();
    }

    public NotificationResponseDTO toResponse(Notification notification) {

        if (notification == null) {
            return null;
        }

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}