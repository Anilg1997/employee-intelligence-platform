package com.employeeintelligence.api.config;

import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.SendResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationKafkaConfigTest {
    @Test
    void rejectsUnboundedRetriesAndUnsafeSuffixes() {
        var config = new NotificationKafkaConfig();
        ConsumerFactory<Object, Object> factory = mock(ConsumerFactory.class);
        KafkaOperations<Object, Object> operations = mock(KafkaOperations.class);
        assertThrows(IllegalArgumentException.class,
                () -> config.notificationKafkaListenerContainerFactory(factory, operations, -1, ".DLT"));
        assertThrows(IllegalArgumentException.class,
                () -> config.notificationKafkaListenerContainerFactory(factory, operations, Long.MAX_VALUE, ".DLT"));
        for (String suffix : new String[]{"", "bad/topic", "\n", "x".repeat(33)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> NotificationKafkaConfig.deadLetterRecoverer(operations, suffix));
        }
        assertEquals(ContainerProperties.AckMode.RECORD,
                config.notificationKafkaListenerContainerFactory(factory, operations, 3, ".DLT")
                        .getContainerProperties().getAckMode());
    }

    @Test
    void failedDltPublicationThrowsRatherThanAcknowledgingRecovery() {
        KafkaOperations<Object, Object> operations = mock(KafkaOperations.class);
        when(operations.send(any(ProducerRecord.class))).thenReturn(
                CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));
        var recoverer = NotificationKafkaConfig.deadLetterRecoverer(operations, ".DLT");
        var record = new ConsumerRecord<Object, Object>("events", 2, 42L, "event-id", "payload");
        assertThrows(RuntimeException.class, () -> recoverer.accept(record, new IllegalStateException("persistence failure")));
        verify(operations).send(any(ProducerRecord.class));
    }

    @Test
    void acknowledgedDltPublicationPreservesPayloadAndPartition() {
        KafkaOperations<Object, Object> operations = mock(KafkaOperations.class);
        when(operations.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        var recoverer = NotificationKafkaConfig.deadLetterRecoverer(operations, ".DLT");
        var record = new ConsumerRecord<Object, Object>("events", 2, 42L, "event-id", "payload");
        assertDoesNotThrow(() -> recoverer.accept(record, new IllegalStateException("persistence failure")));
        var sent = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(operations).send(sent.capture());
        assertEquals("events.DLT", sent.getValue().topic());
        assertEquals(2, sent.getValue().partition());
        assertEquals("event-id", sent.getValue().key());
        assertEquals("payload", sent.getValue().value());
    }
}
