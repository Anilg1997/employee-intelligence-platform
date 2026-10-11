package com.employeeintelligence.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_timestamp", nullable = false) private Instant timestamp;
    @Column(name = "actor_subject", nullable = false) private String actorSubject;
    private String actorRole;
    @Column(nullable = false) private String action;
    private String resourceType;
    private String resourceId;
    @Column(nullable = false) private String result;
    private String correlationId;
    @Column(columnDefinition = "text") private String metadata;

    protected AuditEvent() { }
    public AuditEvent(Instant timestamp, String actorSubject, String actorRole, String action,
                      String resourceType, String resourceId, String result, String correlationId, String metadata) {
        this.timestamp = timestamp; this.actorSubject = actorSubject; this.actorRole = actorRole; this.action = action;
        this.resourceType = resourceType; this.resourceId = resourceId; this.result = result;
        this.correlationId = correlationId; this.metadata = metadata;
    }
    public Long getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getActorSubject() { return actorSubject; }
    public String getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getResult() { return result; }
    public String getCorrelationId() { return correlationId; }
    public String getMetadata() { return metadata; }
}
