package com.employeeintelligence.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uq_notifications_source", columnNames = {"tenant_id", "recipient_subject", "source_event_id"}), indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "tenant_id, recipient_subject, created_at")
})
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false, length = 255) private String tenantId;
    @Column(name = "recipient_subject", nullable = false, length = 255) private String recipientSubject;
    @Column(name = "source_event_id", nullable = false, length = 255) private String sourceEventId;
    @Column(name = "notification_type", nullable = false, length = 100) private String type;
    @Column(nullable = false, length = 255) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "read_at") private Instant readAt;

    protected Notification() { }
    public Notification(String tenantId, String recipientSubject, String sourceEventId, String type, String title, String body) {
        this.sourceEventId = sourceEventId;
        this.tenantId = tenantId; this.recipientSubject = recipientSubject; this.type = type;
        this.title = title; this.body = body; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getSourceEventId() { return sourceEventId; }
    public String getTenantId() { return tenantId; }
    public String getRecipientSubject() { return recipientSubject; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
    public void markRead() { readAt = Instant.now(); }
}
