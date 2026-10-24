package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.DlqReplayRequest;
import com.employeeintelligence.api.model.OutboxEvent;
import com.employeeintelligence.api.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaReplayServiceTest {
    @Test
    void rejectsDlqPayloadsThatAreNotNotificationEnvelopes() {
        var service = new KafkaReplayService(mock(OutboxEventRepository.class), mock(KafkaOperations.class),
                mock(AuditService.class), new ObjectMapper(), "employee-intelligence.events", ".DLT");
        assertThrows(IllegalArgumentException.class, () -> service.replayDlq(
                new DlqReplayRequest("employee-intelligence.events.DLT", "{\"eventType\":\"audit\"}", "k")));
    }

    @Test
    void requeuesOnlyFailedOutboxRowsAndAuditsReplay() {
        var repository = mock(OutboxEventRepository.class);
        var event = mock(OutboxEvent.class);
        when(repository.findById(42L)).thenReturn(Optional.of(event));
        var audit = mock(AuditService.class);
        var service = new KafkaReplayService(repository, mock(KafkaOperations.class), audit,
                new ObjectMapper(), "employee-intelligence.events", ".DLT");
        service.replayOutbox(42L);
        verify(event).requeueForReplay();
        verify(repository).save(event);
        verify(audit).record("KAFKA_OUTBOX_REPLAY", "outbox_event", 42L, "SUCCESS", "requeued failed outbox event");
    }
}
