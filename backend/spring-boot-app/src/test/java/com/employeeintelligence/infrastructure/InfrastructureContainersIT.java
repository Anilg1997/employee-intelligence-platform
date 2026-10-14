package com.employeeintelligence.infrastructure;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Docker-backed smoke checks, excluded from the default unit-test lifecycle. */
@Testcontainers
class InfrastructureContainersIT {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Container static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));
    @Container static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test
    void postgresKafkaAndRedisAcceptSmokeOperations() throws Exception {
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertTrue(connection.isValid(2));
        }
        String topic = "infrastructure-smoke-" + UUID.randomUUID();
        Properties producer = new Properties();
        producer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        producer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        try (var client = new KafkaProducer<String, String>(producer)) {
            client.send(new ProducerRecord<>(topic, "key", "ok")).get(30, java.util.concurrent.TimeUnit.SECONDS);
        }
        Properties consumer = new Properties();
        consumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        consumer.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        consumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        try (var client = new KafkaConsumer<String, String>(consumer)) {
            client.subscribe(java.util.List.of(topic));
            var records = client.poll(Duration.ofSeconds(10));
            assertEquals("ok", records.iterator().next().value());
        }
        try (var socket = new java.net.Socket(REDIS.getHost(), REDIS.getMappedPort(6379))) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write("*1\r\n$4\r\nPING\r\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            assertEquals("+PONG", new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream())).readLine());
        }
    }
}
