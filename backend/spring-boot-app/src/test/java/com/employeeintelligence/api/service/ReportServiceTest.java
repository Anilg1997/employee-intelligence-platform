package com.employeeintelligence.api.service;

import com.employeeintelligence.api.repository.*;
import com.employeeintelligence.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {
    @Test
    void rejectsTargetsThatDoNotMatchTheRequestedReportType() {
        var reports = mock(ReportRepository.class);
        var service = new ReportService(reports, mock(EmployeeRepository.class),
                mock(DashboardService.class), mock(TenantContext.class), new ObjectMapper());
        assertThrows(IllegalArgumentException.class,
                () -> service.generate(new com.employeeintelligence.api.dto.ReportRequest("WORKFORCE", 12L, null)));
        verifyNoInteractions(reports);
    }

    @Test
    void listingReadsOnlyTheCurrentTenant() {
        var reports = mock(ReportRepository.class);
        var tenant = mock(TenantContext.class);
        when(tenant.currentTenantId()).thenReturn("tenant-a");
        when(reports.findAllByTenantId(eq("tenant-a"), any())).thenReturn(new PageImpl<>(List.of()));
        var service = new ReportService(reports, mock(EmployeeRepository.class), mock(DashboardService.class), tenant, new ObjectMapper());
        service.list(10);
        verify(reports).findAllByTenantId(eq("tenant-a"), any());
    }
}
