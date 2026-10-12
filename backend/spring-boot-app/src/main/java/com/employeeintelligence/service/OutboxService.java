package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.AuditEvent;
import com.employeeintelligence.api.model.OutboxEvent;
import com.employeeintelligence.api.notification.NotificationEvent;
import com.employeeintelligence.api.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class OutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void enqueueAudit(AuditEvent audit) {
        try {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("eventId", audit.getId());
            payload.put("tenantId", audit.getTenantId());
            payload.put("timestamp", audit.getTimestamp());
            payload.put("action", audit.getAction());
            payload.put("resourceType", audit.getResourceType());
            payload.put("resourceId", audit.getResourceId());
            payload.put("result", audit.getResult());
            payload.put("correlationId", audit.getCorrelationId());
            // The outbox transports routing/audit facts only; actor identity and
            // free-form metadata remain in the protected audit table.
            repository.save(new OutboxEvent(audit.getTenantId(), "AuditEvent", "audit", audit.getId().toString(), objectMapper.writeValueAsString(payload)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize audit outbox event", exception);
        }
    }

    /**
     * Producer integration point for future domain services. This does not make
     * audit events notification events and is intentionally not called by any
     * current risk, prediction, or reporting service.
     */
    public void enqueueNotification(NotificationEvent event) {
        try {
            repository.save(new OutboxEvent(event.tenantId(), event.eventType().wireValue(),
                    "notification", event.eventId(), objectMapper.writeValueAsString(event.toPayload())));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize notification outbox event", exception);
        }
    }
}
