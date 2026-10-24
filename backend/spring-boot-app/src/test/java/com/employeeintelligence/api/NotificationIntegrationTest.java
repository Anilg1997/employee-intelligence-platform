package com.employeeintelligence.api;

import com.employeeintelligence.api.model.Notification;
import com.employeeintelligence.api.repository.NotificationRepository;
import com.employeeintelligence.api.service.NotificationEventConsumer;
import com.employeeintelligence.api.service.NotificationService;
import com.employeeintelligence.api.agent.EmployeeAiAgent;
import dev.langchain4j.model.chat.ChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real PostgreSQL/Flyway + service/consumer + secured MVC; only external AI/OIDC are mocked.
 * Kafka transport is intentionally not claimed by these tests: onEvent invokes the real consumer directly.
 */
@SpringBootTest(properties = {
        "security.auth.enabled=true", "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.test",
        "notifications.consumer.enabled=false", "outbox.publisher.enabled=false",
        // Allow only the first (empty) scheduled poll, then drive polls synchronously in tests.
        "notifications.sse.poll-ms=86400000"
})
@AutoConfigureMockMvc
class NotificationIntegrationTest {
    private static final String SCHEMA = "notifications_it_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.flyway.schemas", () -> SCHEMA);
        properties.add("spring.flyway.default-schema", () -> SCHEMA);
        properties.add("spring.flyway.baseline-on-migrate", () -> "false");
        properties.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
        properties.add("spring.datasource.hikari.connection-init-sql", () -> "SET search_path TO " + SCHEMA + ", public");
        properties.add("employee.import.enabled", () -> "false");
    }

    @Autowired private NotificationService service;
    @Autowired private NotificationRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private MockMvc mvc;
    @MockitoBean private EmployeeAiAgent employeeAiAgent;
    @MockitoBean private ChatModel chatModel;
    @MockitoBean private JwtDecoder jwtDecoder;

    private final List<MvcResult> streams = new ArrayList<>();
    private NotificationEventConsumer consumer;
    private String tenant;

    @BeforeEach
    void fixture() {
        tenant = "tenant-" + UUID.randomUUID();
        consumer = new NotificationEventConsumer(service);
    }

    @AfterEach
    void disconnectStreams() throws Exception {
        for (var stream : streams) disconnect(stream);
    }

    @AfterAll
    static void removeOwnedSchema(@Autowired DataSource dataSource) {
        // Never delete/truncate public or another suite's schema.
        new JdbcTemplate(dataSource).execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    }

    @Test
    void replayIsIdempotentAndSourceIdentityIsScopedToTenantAndRecipient() throws Exception {
        String payload = event(tenant, "alice", "source-1");
        consumer.onEvent(payload);
        var original = find(tenant, "alice", "source-1");
        consumer.onEvent(payload);
        consumer.onEvent(event(tenant, "bob", "source-1"));
        consumer.onEvent(event(tenant + "-other", "alice", "source-1"));

        assertEquals(original.getId(), find(tenant, "alice", "source-1").getId());
        assertNotEquals(original.getId(), find(tenant, "bob", "source-1").getId());
        assertNotEquals(original.getId(), find(tenant + "-other", "alice", "source-1").getId());
        assertEquals(1, count(tenant, "alice"));
        assertEquals(1, count(tenant, "bob"));
        assertEquals("Risk review available", original.getTitle());
        assertFalse(original.getBody().contains("UNTRUSTED"));

        // Prove the constraint exists in V6, independently of the service's conflict handling.
        assertThrows(DuplicateKeyException.class, () -> jdbc.update("""
                INSERT INTO notifications (tenant_id, recipient_subject, source_event_id, notification_type, title, body)
                VALUES (?, 'alice', 'source-1', 'risk.detected', 'duplicate', 'duplicate')
                """, tenant));
    }

    @Test
    void concurrentDuplicateDeliveriesCommitExactlyOneRow() throws Exception {
        String payload = event(tenant, "alice", "concurrent-source");
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<Future<Long>> deliveries = new ArrayList<>();
            for (int i = 0; i < 4; i++) deliveries.add(pool.submit(() -> {
                await(start);
                consumer.onEvent(payload);
                return find(tenant, "alice", "concurrent-source").getId();
            }));
            start.countDown();
            long id = deliveries.getFirst().get(15, TimeUnit.SECONDS);
            for (var delivery : deliveries) assertEquals(id, delivery.get(15, TimeUnit.SECONDS));
            assertEquals(1, count(tenant, "alice"));
        }
    }

    @Test
    void securedListAndMarkReadCannotReadOrModifyAnotherTenantOrRecipient() throws Exception {
        var own = service.create(tenant, "alice", "own", "risk.detected");
        var otherRecipient = service.create(tenant, "bob", "bob", "risk.detected");
        var otherTenant = service.create(tenant + "-other", "alice", "other", "risk.detected");

        mvc.perform(get("/api/notifications").with(identity(tenant, "alice"))
                        .param("tenantId", tenant + "-other").param("recipientSubject", "bob"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(own.getId()));
        for (long inaccessible : List.of(otherRecipient.getId(), otherTenant.getId(), Long.MAX_VALUE)) {
            mvc.perform(post("/api/notifications/{id}/read", inaccessible).with(identity(tenant, "alice")))
                    .andExpect(status().isNotFound());
        }
        assertNull(repository.findById(otherRecipient.getId()).orElseThrow().getReadAt());
        assertNull(repository.findById(otherTenant.getId()).orElseThrow().getReadAt());
        mvc.perform(post("/api/notifications/{id}/read", own.getId()).with(identity(tenant, "alice")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.readAt").isNotEmpty());
        assertNotNull(repository.findById(own.getId()).orElseThrow().getReadAt());

        // The tenant claim, rather than request parameters, determines the visible rows.
        mvc.perform(get("/api/notifications").with(identity(tenant + "-other", "alice"))
                        .param("tenantId", tenant).param("recipientSubject", "alice"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(otherTenant.getId()));
        mvc.perform(post("/api/notifications/{id}/read", own.getId())
                        .with(identity(tenant + "-other", "alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void secureModeRejectsUnauthenticatedListReadAndStreamEvenWithQueryToken() throws Exception {
        var notification = service.create(tenant, "alice", "unauthenticated", "risk.detected");

        mvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/notifications/{id}/read", notification.getId()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/notifications/stream"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/notifications").param("access_token", "not-a-header-token"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/notifications/{id}/read", notification.getId())
                        .param("access_token", "not-a-header-token"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/notifications/stream").param("access_token", "not-a-header-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowedHrRoleCanListReadAndOpenStream() throws Exception {
        var notification = service.create(tenant, "alice", "hr-access", "risk.detected");

        mvc.perform(get("/api/notifications").with(identity(tenant, "alice")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(notification.getId()));
        mvc.perform(post("/api/notifications/{id}/read", notification.getId()).with(identity(tenant, "alice")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.readAt").isNotEmpty());
        stream("0");
    }

    @Test
    void employeeRoleIsRejectedForEveryNotificationEndpointAndFailureIsAudited() throws Exception {
        var notification = service.create(tenant, "employee", "employee-access", "risk.detected");
        int failuresBefore = jdbc.queryForObject(
                "SELECT count(*) FROM audit_events WHERE tenant_id = ? AND action = 'AUTHORIZATION_FAILURE'",
                Integer.class, tenant);

        mvc.perform(get("/api/notifications").with(identity(tenant, "employee", "EMPLOYEE")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/notifications/{id}/read", notification.getId())
                        .with(identity(tenant, "employee", "EMPLOYEE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/notifications/stream").with(identity(tenant, "employee", "EMPLOYEE")))
                .andExpect(status().isForbidden());

        assertEquals(failuresBefore + 3, jdbc.queryForObject(
                "SELECT count(*) FROM audit_events WHERE tenant_id = ? AND action = 'AUTHORIZATION_FAILURE'",
                Integer.class, tenant));
        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM notifications WHERE id = ? AND read_at IS NOT NULL",
                Integer.class, notification.getId()));
    }

    @Test
    void sseUsesIndividualIdsBoundedPagesAndScopedReplayFromLastEventId() throws Exception {
        var seen = service.create(tenant, "alice", "already-seen", "risk.detected");
        var otherRecipient = service.create(tenant, "bob", "hidden", "risk.detected");
        var otherTenant = service.create(tenant + "-other", "alice", "hidden", "risk.detected");
        List<Long> expected = new ArrayList<>();
        for (int i = 0; i < 103; i++) expected.add(service.create(tenant, "alice", "page-" + i, "report.generated").getId());

        var first = stream(seen.getId().toString());
        service.publishNewNotifications();
        assertEquals(expected.subList(0, 100), eventIds(first));
        assertFalse(eventIds(first).contains(otherRecipient.getId()));
        assertFalse(eventIds(first).contains(otherTenant.getId()));
        String content = first.getResponse().getContentAsString();
        assertTrue(content.contains("event:notification"));
        assertTrue(content.contains("\"id\":" + expected.getFirst()));
        disconnect(first);
        streams.remove(first);

        var resumed = stream(expected.get(99).toString());
        service.publishNewNotifications();
        assertEquals(expected.subList(100, 103), eventIds(resumed));
        service.publishNewNotifications();
        assertEquals(expected.subList(100, 103), eventIds(resumed), "Second poll must not redeliver rows");
        assertTrue(resumed.getResponse().getContentAsString().contains(":keepalive"));
        assertEquals(content, first.getResponse().getContentAsString(), "Disconnected stream must not receive more data");
    }

    @Test
    void uncommittedAndRolledBackRowsNeverReachSseAndRetryCanPersist() throws Exception {
        var open = stream("0");
        var inserted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var pool = Executors.newSingleThreadExecutor()) {
            var writer = pool.submit(() -> new TransactionTemplate(transactions).execute(status -> {
                var row = service.create(tenant, "alice", "rollback-source", "risk.detected");
                inserted.countDown();
                try { await(release); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IllegalStateException(interrupted); }
                status.setRollbackOnly();
                return row.getId();
            }));
            try {
                await(inserted);
                service.publishNewNotifications();
                assertTrue(eventIds(open).isEmpty(), "Polling must not read an uncommitted row");
                assertEquals(0, count(tenant, "alice"));
            } finally { release.countDown(); }
            long rolledBackId = writer.get(15, TimeUnit.SECONDS);
            service.publishNewNotifications();
            assertTrue(eventIds(open).isEmpty(), "Rolled-back writes must never be broadcast");
            assertEquals(0, count(tenant, "alice"));
            consumer.onEvent(event(tenant, "alice", "rollback-source"));
            var committed = find(tenant, "alice", "rollback-source");
            assertNotEquals(rolledBackId, committed.getId());
            service.publishNewNotifications();
            assertEquals(List.of(committed.getId()), eventIds(open));
        }
    }

    @Test
    void recipientWritesWaitForCommitSoCursorCannotSkipAnEarlierTransaction() throws Exception {
        var open = stream("0");
        var inserted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> new TransactionTemplate(transactions).execute(status -> {
                var row = service.create(tenant, "alice", "first-transaction", "risk.detected");
                inserted.countDown();
                try { await(release); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IllegalStateException(interrupted); }
                return row.getId();
            }));
            Future<Long> second;
            try {
                await(inserted);
                second = pool.submit(() -> {
                    secondStarted.countDown();
                    return service.create(tenant, "alice", "second-transaction", "risk.detected").getId();
                });
                await(secondStarted);
                // Observe the second PostgreSQL session actually waiting on the advisory lock,
                // rather than relying on a sleep to infer that the second write was attempted.
                long waitDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                boolean waiting = false;
                while (System.nanoTime() < waitDeadline && !second.isDone()) {
                    waiting = Boolean.TRUE.equals(jdbc.queryForObject("""
                            SELECT EXISTS (SELECT 1 FROM pg_stat_activity
                              WHERE datname = current_database() AND wait_event = 'advisory'
                              AND query LIKE '%pg_advisory_xact_lock%')
                            """, Boolean.class));
                    if (waiting) break;
                    Thread.sleep(10);
                }
                assertTrue(waiting, "The next recipient write must wait for the earlier transaction to commit");
                service.publishNewNotifications();
                assertTrue(eventIds(open).isEmpty());
            } finally { release.countDown(); }
            long firstId = first.get(15, TimeUnit.SECONDS);
            long secondId = second.get(15, TimeUnit.SECONDS);
            assertTrue(firstId < secondId);
            service.publishNewNotifications();
            assertEquals(List.of(firstId, secondId), eventIds(open));
        }
    }

    @Test
    void streamRequiresHeaderAuthenticationAndRejectsInvalidCursorAndMissingTenant() throws Exception {
        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/notifications/1/read")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/notifications/stream")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/notifications/stream").param("access_token", "not-a-header-token"))
                .andExpect(status().isUnauthorized());
        for (String invalidCursor : List.of("-1", "abc", "1.5", "12345678901234567890")) {
            mvc.perform(get("/api/notifications/stream").with(identity(tenant, "alice"))
                            .header("Last-Event-ID", invalidCursor))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/notifications").with(jwt().jwt(j -> j.subject("alice"))
                        .authorities(() -> "ROLE_HR_ANALYST")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/notifications").with(jwt().jwt(j -> j.subject("alice").claim("tenant_id", tenant))
                        .authorities(() -> "ROLE_EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamLifetimeIsCappedAtFiveMinutesAndAtJwtExpiry() throws Exception {
        var capped = stream("0");
        long timeout = capped.getRequest().getAsyncContext().getTimeout();
        assertTrue(timeout > 0 && timeout <= 300_000);
        Instant expiry = Instant.now().plusSeconds(30);
        var shorter = mvc.perform(get("/api/notifications/stream").with(jwt().jwt(j -> j.subject("alice")
                                .claim("tenant_id", tenant).expiresAt(expiry)).authorities(() -> "ROLE_HR_ANALYST")))
                .andExpect(request().asyncStarted()).andReturn();
        streams.add(shorter);
        long shorterTimeout = shorter.getRequest().getAsyncContext().getTimeout();
        assertTrue(shorterTimeout > 0 && shorterTimeout <= 30_000);
        mvc.perform(get("/api/notifications/stream").with(jwt().jwt(j -> j.subject("alice")
                                .claim("tenant_id", tenant).expiresAt(Instant.now().minusSeconds(1)))
                        .authorities(() -> "ROLE_HR_ANALYST")))
                .andExpect(status().isBadRequest()).andExpect(request().asyncNotStarted());
    }

    private Notification find(String tenantId, String subject, String source) {
        return repository.findByTenantIdAndRecipientSubjectAndSourceEventId(tenantId, subject, source).orElseThrow();
    }

    private int count(String tenantId, String subject) {
        return jdbc.queryForObject("SELECT count(*) FROM notifications WHERE tenant_id = ? AND recipient_subject = ?", Integer.class, tenantId, subject);
    }

    private String event(String tenantId, String subject, String source) throws Exception {
        return new ObjectMapper().writeValueAsString(Map.of("schemaVersion", 1, "tenantId", tenantId, "recipientSubject", subject,
                "eventId", source, "eventType", "risk.detected", "title", "UNTRUSTED title", "body", "UNTRUSTED body"));
    }

    private RequestPostProcessor identity(String tenantId, String subject) {
        return identity(tenantId, subject, "HR_ANALYST");
    }

    private RequestPostProcessor identity(String tenantId, String subject, String role) {
        return jwt().jwt(j -> j.subject(subject).claim("tenant_id", tenantId)
                .expiresAt(Instant.now().plusSeconds(600))).authorities(() -> "ROLE_" + role);
    }

    private MvcResult stream(String cursor) throws Exception {
        var result = mvc.perform(get("/api/notifications/stream").with(identity(tenant, "alice"))
                        .header("Last-Event-ID", cursor))
                .andExpect(status().isOk()).andExpect(request().asyncStarted()).andReturn();
        streams.add(result);
        return result;
    }

    private void disconnect(MvcResult result) throws Exception {
        var context = (MockAsyncContext) result.getRequest().getAsyncContext();
        for (var listener : context.getListeners()) listener.onComplete(new AsyncEvent(context));
    }

    private static List<Long> eventIds(MvcResult result) throws Exception {
        var matcher = Pattern.compile("(?m)^id:(\\d+)\\r?$").matcher(result.getResponse().getContentAsString());
        List<Long> ids = new ArrayList<>();
        while (matcher.find()) ids.add(Long.valueOf(matcher.group(1)));
        return ids;
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("Timed out awaiting test transaction");
    }
}
