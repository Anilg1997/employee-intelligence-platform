package com.employeeintelligence.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.core.KafkaOperations;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "notifications.consumer.enabled", havingValue = "true")
public class NotificationKafkaConfig {
    ConcurrentKafkaListenerContainerFactory<Object, Object> notificationKafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory, KafkaOperations<Object, Object> kafkaOperations,
            long maxRetries, String deadLetterSuffix) {
        return notificationKafkaListenerContainerFactory(consumerFactory, kafkaOperations, maxRetries, 1000L, deadLetterSuffix);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> notificationKafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
             KafkaOperations<Object, Object> kafkaOperations,
             @org.springframework.beans.factory.annotation.Value("${notifications.consumer.max-retries:3}") long maxRetries,
             @org.springframework.beans.factory.annotation.Value("${notifications.consumer.retry-backoff-ms:1000}") long retryBackoffMs,
             @org.springframework.beans.factory.annotation.Value("${notifications.consumer.dead-letter-suffix:.DLT}") String deadLetterSuffix) {
        if (maxRetries < 0 || maxRetries > 100 || retryBackoffMs < 0 || retryBackoffMs > 300000) {
            throw new IllegalArgumentException("Notification retries must be between 0 and 100");
        }
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        var recoverer = deadLetterRecoverer(kafkaOperations, deadLetterSuffix);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(retryBackoffMs, maxRetries));
        factory.setCommonErrorHandler(handler);
        return factory;
    }

    static DeadLetterPublishingRecoverer deadLetterRecoverer(KafkaOperations<Object, Object> operations, String suffix) {
        if (suffix == null || !suffix.matches("[A-Za-z0-9._-]{1,32}")) {
            throw new IllegalArgumentException("Dead-letter suffix must contain 1-32 Kafka topic characters");
        }
        var recoverer = new DeadLetterPublishingRecoverer(operations, (record, exception) -> {
            String topic = record.topic() + suffix;
            if (topic.length() > 249) throw new IllegalArgumentException("Dead-letter topic exceeds Kafka's name limit");
            return new TopicPartition(topic, record.partition());
        });
        // A failed DLT send must throw, retaining the source record for retry.
        recoverer.setFailIfSendResultIsError(true);
        return recoverer;
    }
}
