package com.employeeintelligence.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "notifications.consumer.enabled", havingValue = "true")
public class NotificationKafkaConfig {
    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> notificationKafkaListenerContainerFactory(ConsumerFactory<Object, Object> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        var handler = new DefaultErrorHandler(new FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        factory.setCommonErrorHandler(handler);
        return factory;
    }
}
