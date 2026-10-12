package com.employeeintelligence.api.service;

import com.employeeintelligence.api.notification.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/** Only approved event metadata is accepted; event-provided prose is never displayed. */
@Service
@ConditionalOnProperty(name = "notifications.consumer.enabled", havingValue = "true")
public class NotificationEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this(notificationService, new ObjectMapper());
    }

    public NotificationEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${outbox.kafka.topic:employee-intelligence.events}", groupId = "${notifications.consumer.group:notifications-v1}", containerFactory = "notificationKafkaListenerContainerFactory")
    public void onEvent(String payload) {
        var event = NotificationEvent.parse(payload, objectMapper);
        if (event.isEmpty()) return;
        // Persistence errors must reach Kafka's retry handler, never acknowledge a lost write.
        var value = event.get();
        notificationService.create(value.tenantId(), value.recipientSubject(), value.eventId(), value.eventType().wireValue());
    }
}
