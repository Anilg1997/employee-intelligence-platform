package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.AuditEvent;
import com.employeeintelligence.api.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Owns the audit transaction so best-effort error handling cannot swallow a rollback. */
@Service
public class AuditWriter {
    private final AuditEventRepository repository;
    private final OutboxService outboxService;

    public AuditWriter(AuditEventRepository repository, OutboxService outboxService) {
        this.repository = repository;
        this.outboxService = outboxService;
    }

    @Transactional
    public void write(AuditEvent event) {
        repository.save(event);
        outboxService.enqueueAudit(event);
    }
}
