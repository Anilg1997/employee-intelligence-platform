package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Report;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findAllByTenantId(String tenantId, Pageable pageable);
    Optional<Report> findByIdAndTenantId(Long id, String tenantId);
}
