package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.OutboxEvent;
import com.employeeintelligence.api.notification.NotificationEvent;
import com.employeeintelligence.api.notification.NotificationEventType;
import com.employeeintelligence.api.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxServiceNotificationContractTest {
    @Test
    void exposesFutureNotificationProducerIntegrationPointWithoutDisplayFields() {
        var repository = mock(OutboxEventRepository.class);
        var service = new OutboxService(repository);
        service.enqueueNotification(new NotificationEvent(1, NotificationEventType.REPORT_GENERATED,
                "report-1", "tenant-a", "subject-a"));

        var captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        var outbox = captor.getValue();
        assertEquals("report.generated", outbox.getEventType());
        assertEquals("notification", outbox.getAggregateType());
        assertEquals("report-1", outbox.getAggregateId());
        assertTrue(outbox.getPayload().contains("\"schemaVersion\":1"));
        assertTrue(outbox.getPayload().contains("\"recipientSubject\":\"subject-a\""));
        assertFalse(outbox.getPayload().contains("title"));
        assertFalse(outbox.getPayload().contains("body"));
    }
}
