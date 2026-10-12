package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(cacheNames = "dashboard", keyGenerator = "tenantCacheKeyGenerator")
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

        long labeled = employees.stream().filter(e -> e.getAttrition() != null && !e.getAttrition().isBlank()).count();
        long attritionCount = employees.stream().filter(e -> "yes".equalsIgnoreCase(e.getAttrition())).count();
        Double attritionRate = labeled == 0 ? null : (double) attritionCount / labeled;
        Double averageAge = average(employees.stream().map(Employee::getAge).toList());
        Double averageMonthlyIncome = average(employees.stream().map(Employee::getMonthlyIncome).toList());

        return new DashboardResponse(
                employees.size(),
                departmentStatistics, labeled, attritionCount, attritionRate,
                averageAge, averageMonthlyIncome
        );
    }

    private static Double average(java.util.List<Integer> values) {
        return values.stream().filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average()
                .stream().boxed().findFirst().orElse(null);
    }
}
