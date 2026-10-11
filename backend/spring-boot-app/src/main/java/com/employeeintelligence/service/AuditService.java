package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.AuditEvent;
import com.employeeintelligence.api.repository.AuditEventRepository;
import com.employeeintelligence.security.AuthenticatedUser;
import com.employeeintelligence.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service;
import java.time.Instant; import java.util.UUID;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository repository;
    private final TenantContext tenantContext;
    public AuditService(AuditEventRepository repository, TenantContext tenantContext) { this.repository = repository; this.tenantContext = tenantContext; }
    public void record(String action, String resourceType, Object resourceId, String result, String metadata) {
        try {
            var user = AuthenticatedUser.current();
            String role = user.roles().stream().findFirst().orElse(null);
            AuditEvent event = new AuditEvent(Instant.now(), safe(user.subject()), safe(role), safe(action), safe(resourceType), resourceId == null ? null : safe(String.valueOf(resourceId)), safe(result), UUID.randomUUID().toString(), sanitize(metadata));
            event.setTenantId(tenantContext.currentTenantId()); repository.save(event);
        } catch (Exception ex) { log.warn("Audit persistence failed; continuing primary operation", ex); }
    }
    public void record(String action, String resourceType, Object resourceId, String result, String metadata, HttpServletRequest request) {
        record(action, resourceType, resourceId, result, sanitize(metadata));
    }
    public Page<AuditEvent> find(String action, String result, Pageable pageable) {
        return repository.findByTenantIdAndActionContainingIgnoreCaseAndResultContainingIgnoreCase(tenantContext.currentTenantId(), action == null ? "" : action, result == null ? "" : result, pageable);
    }
    private static String safe(String value) { return value == null ? null : value.length() > 255 ? value.substring(0, 255) : value; }
    private static String sanitize(String value) {
        if (value == null) return null;
        String lower = value.toLowerCase();
        if (lower.contains("prompt") || lower.contains("token") || lower.contains("secret") || lower.contains("password")) return "[redacted]";
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
