package com.employeeintelligence.api.dto;

import com.employeeintelligence.api.model.Notification;
import java.time.Instant;

public record NotificationResponse(Long id, String type, String title, String body,
                                   Instant createdAt, Instant readAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getBody(), notification.getCreatedAt(), notification.getReadAt());
    }
}
