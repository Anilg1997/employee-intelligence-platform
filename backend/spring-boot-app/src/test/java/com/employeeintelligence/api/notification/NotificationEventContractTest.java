package com.employeeintelligence.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventContractTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripsOnlyVersionedRoutingIdentityAndNeverDisplayProse() throws Exception {
        var event = new NotificationEvent(1, NotificationEventType.RISK_DETECTED, "event-1", "tenant-a", "subject-a");
        var payload = mapper.writeValueAsString(event.toPayload());
        var parsed = NotificationEvent.parse(payload, mapper).orElseThrow();

        assertEquals(event, parsed);
        assertFalse(payload.contains("title"));
        assertFalse(payload.contains("body"));
    }

    @Test
    void rejectsAuditOnlyPayloadsUnsupportedVersionsAndNumericIdentities() throws Exception {
        var audit = mapper.writeValueAsString(Map.of("eventId", 7, "tenantId", "tenant-a",
                "action", "LOGIN", "resourceType", "audit"));
        var unsupported = mapper.writeValueAsString(Map.of("schemaVersion", 2, "eventType", "risk.detected",
                "eventId", "e", "tenantId", "t", "recipientSubject", "u"));
        var numericIdentity = mapper.writeValueAsString(Map.of("schemaVersion", 1, "eventType", "risk.detected",
                "eventId", 7, "tenantId", "t", "recipientSubject", "u"));

        assertTrue(NotificationEvent.parse(audit, mapper).isEmpty());
        assertTrue(NotificationEvent.parse(unsupported, mapper).isEmpty());
        assertTrue(NotificationEvent.parse(numericIdentity, mapper).isEmpty());
    }

    @Test
    void rejectsUnallowlistedTypeAndInvalidIdentity() throws Exception {
        var unallowlisted = mapper.writeValueAsString(Map.of("schemaVersion", 1, "eventType", "audit.created",
                "eventId", "e", "tenantId", "t", "recipientSubject", "u"));
        var invalid = mapper.writeValueAsString(Map.of("schemaVersion", 1, "eventType", "risk.detected",
                "eventId", "e", "tenantId", "t", "recipientSubject", "u\nattacker"));

        assertTrue(NotificationEvent.parse(unallowlisted, mapper).isEmpty());
        assertTrue(NotificationEvent.parse(invalid, mapper).isEmpty());
    }
}
