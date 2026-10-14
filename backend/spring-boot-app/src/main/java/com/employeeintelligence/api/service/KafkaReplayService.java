package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.DlqReplayRequest;
import com.employeeintelligence.api.model.OutboxEvent;
import com.employeeintelligence.api.notification.NotificationEvent;
import com.employeeintelligence.api.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaReplayService {
    private final OutboxEventRepository outbox;
    private final KafkaOperations<String, String> kafka;
    private final AuditService audit;
    private final ObjectMapper mapper;
    private final String topic;
    private final String suffix;

    public KafkaReplayService(OutboxEventRepository outbox, KafkaOperations<String, String> kafka,
                              AuditService audit, ObjectMapper mapper,
                              @Value("${outbox.kafka.topic:employee-intelligence.events}") String topic,
                              @Value("${notifications.consumer.dead-letter-suffix:.DLT}") String suffix) {
        this.outbox = outbox; this.kafka = kafka; this.audit = audit; this.mapper = mapper;
        this.topic = topic; this.suffix = suffix;
        if (suffix == null || !suffix.matches("[A-Za-z0-9._-]{1,32}")) throw new IllegalArgumentException("Invalid DLQ suffix");
    }

    @Transactional
    public void replayOutbox(Long id) {
        OutboxEvent event = outbox.findById(id).orElseThrow(() -> new IllegalArgumentException("Outbox event not found"));
        event.requeueForReplay();
        outbox.save(event);
        audit.record("KAFKA_OUTBOX_REPLAY", "outbox_event", id, "SUCCESS", "requeued failed outbox event");
    }

    public void replayDlq(DlqReplayRequest request) {
        if (!request.deadLetterTopic().equals(topic + suffix)) throw new IllegalArgumentException("DLQ topic is not configured for notifications");
        if (NotificationEvent.parse(request.payload(), mapper).isEmpty()) throw new IllegalArgumentException("DLQ payload is not a valid notification event");
        try {
            kafka.send(topic, request.key(), request.payload()).get(5, TimeUnit.SECONDS);
        } catch (Exception failure) {
            throw new IllegalStateException("Kafka replay publish failed", failure);
        }
        audit.record("KAFKA_DLQ_REPLAY", "kafka_topic", request.deadLetterTopic(), "SUCCESS", "replayed validated notification event");
    }
}
