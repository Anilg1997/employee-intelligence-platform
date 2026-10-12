package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {
    @Test
    void aggregatesOnlyStoredAttritionLabelsAndNonEmptyNumericValues() {
        Employee exited = employee("Sales", "Yes", 30, 3000);
        Employee retained = employee("Sales", "No", 40, 5000);
        Employee unlabeled = employee("Engineering", null, null, null);
        EmployeeRepository repository = mock(EmployeeRepository.class);
        TenantContext tenantContext = mock(TenantContext.class);
        when(tenantContext.currentTenantId()).thenReturn("tenant-a");
        when(repository.findAllByTenantId("tenant-a")).thenReturn(List.of(exited, retained, unlabeled));

        var response = new DashboardService(repository, tenantContext).getDashboardSummary();

        assertEquals(3, response.getTotalEmployees());
        assertEquals(2, response.getAttritionLabeledEmployees());
        assertEquals(1, response.getAttritionCount());
        assertEquals(0.5, response.getAttritionRate());
        assertEquals(35.0, response.getAverageAge());
        assertEquals(4000.0, response.getAverageMonthlyIncome());
        assertEquals(2L, response.getDepartmentStatistics().get("Sales"));
    }

    private static Employee employee(String department, String attrition, Integer age, Integer income) {
        Employee employee = new Employee();
        employee.setDepartment(department);
        employee.setAttrition(attrition);
        employee.setAge(age);
        employee.setMonthlyIncome(income);
        return employee;
    }
}
