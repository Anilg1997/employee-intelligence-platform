package com.employeeintelligence.api.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationEventConsumerTest {
    @Test
    void rejectsUnallowlistedAndMissingIdentityWithoutPersistence() {
        var service = mock(NotificationService.class);
        var consumer = new NotificationEventConsumer(service);
        consumer.onEvent("{\"schemaVersion\":1,\"eventType\":\"audit.created\",\"tenantId\":\"t\",\"recipientSubject\":\"u\",\"eventId\":\"e\"}");
        consumer.onEvent("{\"schemaVersion\":1,\"eventType\":\"risk.detected\",\"tenantId\":\"t\",\"recipientSubject\":\"u\"}");
        consumer.onEvent("{\"eventId\":\"audit-id\",\"tenantId\":\"t\",\"action\":\"LOGIN\",\"resourceType\":\"audit\"}");
        verifyNoInteractions(service);
    }

    @Test
    void propagatesPersistenceFailureForKafkaRetry() {
        var service = mock(NotificationService.class);
        doThrow(new RuntimeException("database unavailable")).when(service).create("t", "u", "e", "risk.detected");
        var consumer = new NotificationEventConsumer(service);
        assertThrows(RuntimeException.class, () -> consumer.onEvent("{\"schemaVersion\":1,\"eventType\":\"risk.detected\",\"tenantId\":\"t\",\"recipientSubject\":\"u\",\"eventId\":\"e\"}"));
    }
}
