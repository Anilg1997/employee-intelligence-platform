package com.employeeintelligence.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_pending", columnList = "published_at, created_at")
})
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 255)
    private String tenantId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 255)
    private String aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    protected OutboxEvent() { }

    public OutboxEvent(String tenantId, String eventType, String aggregateType,
                       String aggregateId, String payload) {
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }

    public void markPublished() {
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markFailed(Exception exception) {
        this.attempts++;
        // Keep broker responses and event payloads out of the database and logs.
        this.lastError = exception.getClass().getSimpleName();
    }
}
