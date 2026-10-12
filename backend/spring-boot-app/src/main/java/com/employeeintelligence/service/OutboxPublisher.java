package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.OutboxEvent;
import com.employeeintelligence.api.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "outbox.publisher.enabled", havingValue = "true")
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final int batchSize;
    private final int maxRetries;
    private final long ackTimeoutMs;
    private final long retryBackoffMs;

    public OutboxPublisher(OutboxEventRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${outbox.kafka.topic:employee-intelligence.events}") String topic,
                           @Value("${outbox.publisher.batch-size:100}") int batchSize,
                           @Value("${outbox.publisher.max-retries:3}") int maxRetries,
                           @Value("${outbox.publisher.ack-timeout-ms:5000}") long ackTimeoutMs,
                           @Value("${outbox.publisher.retry-backoff-ms:100}") long retryBackoffMs) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.batchSize = Math.max(1, batchSize);
        this.maxRetries = Math.max(1, maxRetries);
        this.ackTimeoutMs = Math.max(1, ackTimeoutMs);
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.lockPending(batchSize)) {
            Exception failure = null;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    kafkaTemplate.send(topic, String.valueOf(event.getId()), event.getPayload())
                            .get(ackTimeoutMs, TimeUnit.MILLISECONDS);
                    event.markPublished();
                    failure = null;
                    break;
                } catch (Exception ex) {
                    failure = ex;
                    if (attempt < maxRetries && retryBackoffMs > 0) {
                        try { Thread.sleep(retryBackoffMs); }
                        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); break; }
                    }
                }
            }
            if (failure != null) {
                event.markFailed(failure);
                log.warn("Outbox event {} publish failed after {} attempt(s); retrying later", event.getId(), event.getAttempts());
            }
            repository.save(event);
        }
    }
}
