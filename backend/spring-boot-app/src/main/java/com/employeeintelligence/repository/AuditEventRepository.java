package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findByActionContainingIgnoreCaseAndResultContainingIgnoreCase(
            String action, String result, Pageable pageable);
}
