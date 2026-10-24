package com.employeeintelligence.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.employeeintelligence.api.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class Employee360ServiceTest {
    @Mock EmployeeService employeeService;
    @Mock AuditService auditService;
    @Mock RiskSummaryService riskSummaryService;
    @InjectMocks Employee360Service service;

    @Test
    void boundsTimelineAndLoadsAllThreeTenantScopedSlices() {
        Employee employee = new Employee("Anil", "IT", "Developer", 30);
        when(employeeService.getEmployeeById(7L)).thenReturn(employee);
        when(auditService.findForEmployee(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(riskSummaryService.summarize(7L)).thenReturn(null);

        var response = service.getEmployee360(7L, 1000);

        assertEquals("Anil", response.employee().name());
        verify(auditService).findForEmployee(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.argThat(
                page -> page.getPageSize() == 100 && page.getPageNumber() == 0));
        verify(riskSummaryService).summarize(7L);
    }

    @Test
    void doesNotReadAuditOrRiskWhenEmployeeIsMissing() {
        when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException(99L));
        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployee360(99L, 25));
        verify(auditService, never()).findForEmployee(any(), any(Pageable.class));
        verify(riskSummaryService, never()).summarize(99L);
    }
}
