package com.employeeintelligence.api.service;

import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {
    @Test
    void aggregatesOnlyStoredAttritionLabelsAndNonEmptyNumericValues() {
        EmployeeRepository repository = mock(EmployeeRepository.class);
        TenantContext tenantContext = mock(TenantContext.class);
        when(tenantContext.currentTenantId()).thenReturn("tenant-a");
        var sales = workforce("Sales", 2, 1, 1, 2, 70, 2, 8000);
        var engineering = workforce("Engineering", 1, 0, 0, 0, 0, 0, 0);
        var rating3 = performance(3, 1);
        var rating4 = performance(4, 1);
        when(repository.summarizeWorkforce("tenant-a")).thenReturn(List.of(sales, engineering));
        when(repository.summarizePerformance("tenant-a")).thenReturn(List.of(rating3, rating4));

        var response = new DashboardService(repository, tenantContext).getDashboardSummary();

        assertEquals(3, response.getTotalEmployees());
        assertEquals(2, response.getAttritionLabeledEmployees());
        assertEquals(1, response.getAttritionCount());
        assertEquals(0.5, response.getAttritionRate());
        assertEquals(35.0, response.getAverageAge());
        assertEquals(4000.0, response.getAverageMonthlyIncome());
        assertEquals(2L, response.getDepartmentStatistics().get("Sales"));
        assertEquals(1L, response.getAttritionDistribution().get("Yes"));
        assertEquals(1L, response.getAttritionDistribution().get("No"));
        assertEquals(1L, response.getAttritionDistribution().get("Unlabeled"));
        assertEquals(1L, response.getPerformanceDistribution().get("3"));
        assertEquals(2, response.getDepartmentSummaries().size());
        assertTrue(response.getSnapshotMetadata().isDecisionSupportOnly());
    }

    private static EmployeeRepository.WorkforceAggregate workforce(String department, long count, long yes,
            long no, long ageCount, long ageTotal, long incomeCount, long incomeTotal) {
        var aggregate = mock(EmployeeRepository.WorkforceAggregate.class);
        when(aggregate.getDepartment()).thenReturn(department);
        when(aggregate.getEmployeeCount()).thenReturn(count);
        when(aggregate.getAttritionCount()).thenReturn(yes);
        when(aggregate.getRetainedCount()).thenReturn(no);
        when(aggregate.getAgeCount()).thenReturn(ageCount);
        when(aggregate.getAgeTotal()).thenReturn(ageTotal);
        when(aggregate.getIncomeCount()).thenReturn(incomeCount);
        when(aggregate.getIncomeTotal()).thenReturn(incomeTotal);
        return aggregate;
    }

    private static EmployeeRepository.PerformanceAggregate performance(Integer rating, long count) {
        var aggregate = mock(EmployeeRepository.PerformanceAggregate.class);
        when(aggregate.getRating()).thenReturn(rating);
        when(aggregate.getEmployeeCount()).thenReturn(count);
        return aggregate;
    }
}
