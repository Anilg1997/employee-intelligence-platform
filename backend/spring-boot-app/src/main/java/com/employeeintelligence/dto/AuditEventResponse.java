package com.employeeintelligence.api.dto;

import com.employeeintelligence.api.model.AuditEvent;
import java.time.Instant;

public record AuditEventResponse(Long id, Instant timestamp, String actorSubject, String actorRole,
        String action, String resourceType, String resourceId, String result, String correlationId, String metadata) {
    public static AuditEventResponse from(AuditEvent e) { return new AuditEventResponse(e.getId(), e.getTimestamp(), e.getActorSubject(), e.getActorRole(), e.getAction(), e.getResourceType(), e.getResourceId(), e.getResult(), e.getCorrelationId(), e.getMetadata()); }
}
