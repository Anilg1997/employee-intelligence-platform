package com.employeeintelligence.api.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.employeeintelligence.api.repository.AuditEventRepository;
import com.employeeintelligence.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuditServiceTenantTest {
    @Mock AuditEventRepository repository;
    @Mock TenantContext tenantContext;
    @Mock AuditWriter auditWriter;

    @Test
    void employeeTimelineAlwaysIncludesCurrentTenantInRepositoryQuery() {
        when(tenantContext.currentTenantId()).thenReturn("tenant-a");
        when(repository.findByTenantIdAndResourceTypeAndResourceId(eq("tenant-a"), eq("employee"),
                eq("7"), org.mockito.ArgumentMatchers.any())).thenReturn(new PageImpl<>(List.of()));

        new AuditService(repository, tenantContext, auditWriter)
                .findForEmployee(7L, PageRequest.of(0, 25));

        verify(repository).findByTenantIdAndResourceTypeAndResourceId(eq("tenant-a"), eq("employee"),
                eq("7"), org.mockito.ArgumentMatchers.any());
    }
}
