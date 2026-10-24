package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTenantIdAndRecipientSubjectOrderByCreatedAtDesc(String tenantId, String subject, Pageable pageable);
    List<Notification> findByTenantIdAndRecipientSubjectAndIdGreaterThanOrderByIdAsc(String tenantId, String subject, Long id, Pageable pageable);
    Optional<Notification> findByTenantIdAndRecipientSubjectAndSourceEventId(String tenantId, String subject, String sourceEventId);
    Optional<Notification> findByIdAndTenantIdAndRecipientSubject(Long id, String tenantId, String subject);
}
