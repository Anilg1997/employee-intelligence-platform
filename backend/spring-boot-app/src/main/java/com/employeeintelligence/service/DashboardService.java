package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import com.employeeintelligence.security.TenantContext;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public DashboardService(EmployeeRepository employeeRepository, TenantContext tenantContext) {
        this.employeeRepository = employeeRepository; this.tenantContext = tenantContext;
    }

    public DashboardResponse getDashboardSummary() {

        var employees = employeeRepository.findAllByTenantId(tenantContext.currentTenantId());

        Map<String, Long> departmentStatistics =
                employees.stream()
                        .map(Employee::getDepartment)
                        .filter(department -> department != null && !department.isBlank())
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        ));

        return new DashboardResponse(
                employees.size(),
                departmentStatistics
        );
    }
}
