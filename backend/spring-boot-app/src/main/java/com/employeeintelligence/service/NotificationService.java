package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.NotificationResponse;
import com.employeeintelligence.api.model.Notification;
import com.employeeintelligence.api.repository.NotificationRepository;
import com.employeeintelligence.security.AuthenticatedUser;
import com.employeeintelligence.security.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {
    private static final int PAGE_SIZE = 100;
    private static final long MAX_LIFETIME_MS = 5 * 60 * 1000L;
    private static final Map<String, String> TITLES = Map.of(
            "risk.detected", "Risk review available",
            "prediction.completed", "Prediction completed",
            "report.generated", "Report available");
    private final NotificationRepository repository;
    private final TenantContext tenantContext;
    private final JdbcTemplate jdbc;
    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public NotificationService(NotificationRepository repository, TenantContext tenantContext, JdbcTemplate jdbc) {
        this.repository = repository; this.tenantContext = tenantContext; this.jdbc = jdbc;
    }

    public static boolean supportedType(String type) { return type != null && TITLES.containsKey(type); }

    public List<NotificationResponse> recent(int size) {
        return repository.findByTenantIdAndRecipientSubjectOrderByCreatedAtDesc(
                tenantContext.currentTenantId(), AuthenticatedUser.current().subject(),
                PageRequest.of(0, Math.min(Math.max(size, 1), PAGE_SIZE)))
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        var notification = repository.findByIdAndTenantIdAndRecipientSubject(id, tenantContext.currentTenantId(), AuthenticatedUser.current().subject())
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        notification.markRead();
        return NotificationResponse.from(repository.save(notification));
    }

    @Transactional
    public Notification create(String tenantId, String recipientSubject, String sourceEventId, String type) {
        if (!supportedType(type)) throw new IllegalArgumentException("Unsupported notification type");
        validateIdentity(tenantId); validateIdentity(recipientSubject); validateIdentity(sourceEventId);
        // Serialize this recipient's writers until commit. Sequence allocation alone does
        // not imply commit order and could otherwise make an SSE cursor skip a late commit.
        jdbc.execute("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (PreparedStatementCallback<Boolean>) statement -> {
            statement.setString(1, tenantId.length() + ":" + tenantId + recipientSubject);
            return statement.execute();
        });
        jdbc.update("""
                INSERT INTO notifications (tenant_id, recipient_subject, source_event_id, notification_type, title, body)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (tenant_id, recipient_subject, source_event_id) DO NOTHING
                """, tenantId, recipientSubject, sourceEventId, type, TITLES.get(type),
                "A new update is available. Open the relevant workspace to review it.");
        return repository.findByTenantIdAndRecipientSubjectAndSourceEventId(tenantId, recipientSubject, sourceEventId).orElseThrow();
    }

    private static void validateIdentity(String value) {
        if (value == null || value.isBlank() || value.length() > 255 || value.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid notification identity");
    }

    public SseEmitter subscribe(String cursor) {
        long lastId = parseCursor(cursor);
        long now = System.currentTimeMillis();
        long deadline = now + MAX_LIFETIME_MS;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            Instant expiry = jwt.getToken().getExpiresAt();
            if (expiry == null || expiry.toEpochMilli() <= now) throw new IllegalArgumentException("JWT expiry required for stream");
            deadline = Math.min(deadline, expiry.toEpochMilli());
        }
        var subscription = new Subscription(tenantContext.currentTenantId(), AuthenticatedUser.current().subject(), lastId, deadline);
        subscription.emitter.onCompletion(() -> subscriptions.remove(subscription));
        subscription.emitter.onTimeout(() -> close(subscription, null));
        subscription.emitter.onError(error -> close(subscription, error));
        subscriptions.add(subscription);
        // No initial entity broadcast: only the poller reads committed database rows.
        return subscription.emitter;
    }

    static long parseCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) return 0L;
        try {
            if (!cursor.matches("[0-9]{1,19}")) throw new NumberFormatException();
            return Long.parseLong(cursor);
        } catch (NumberFormatException invalid) { throw new IllegalArgumentException("Invalid Last-Event-ID"); }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${notifications.sse.poll-ms:2000}")
    public void publishNewNotifications() {
        subscriptions.forEach(subscription -> {
            synchronized (subscription) {
                if (subscription.closed) return;
                if (System.currentTimeMillis() >= subscription.deadline) { close(subscription, null); return; }
                try {
                    var rows = repository.findByTenantIdAndRecipientSubjectAndIdGreaterThanOrderByIdAsc(
                            subscription.tenantId, subscription.subject, subscription.lastId, PageRequest.of(0, PAGE_SIZE));
                    for (var row : rows) {
                        if (System.currentTimeMillis() >= subscription.deadline) { close(subscription, null); return; }
                        subscription.emitter.send(SseEmitter.event().id(row.getId().toString()).name("notification").data(NotificationResponse.from(row)));
                        subscription.lastId = row.getId();
                    }
                    if (rows.isEmpty()) subscription.emitter.send(SseEmitter.event().comment("keepalive"));
                } catch (IOException | RuntimeException error) { close(subscription, error); }
            }
        });
    }

    private void close(Subscription subscription, Throwable error) {
        synchronized (subscription) {
            if (subscription.closed) return;
            subscription.closed = true;
            subscriptions.remove(subscription);
            if (error == null) subscription.emitter.complete();
            else subscription.emitter.completeWithError(error);
        }
    }

    private static final class Subscription {
        private final String tenantId;
        private final String subject;
        private final SseEmitter emitter;
        private final long deadline;
        private long lastId;
        private boolean closed;
        private Subscription(String tenantId, String subject, long lastId, long deadline) {
            this.tenantId = tenantId; this.subject = subject; this.lastId = lastId; this.deadline = deadline;
            this.emitter = new SseEmitter(Math.max(1L, deadline - System.currentTimeMillis()));
        }
    }
}
