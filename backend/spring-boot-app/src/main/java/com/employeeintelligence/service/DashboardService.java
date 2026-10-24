package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.security.TenantContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardService {
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public DashboardService(EmployeeRepository employeeRepository, TenantContext tenantContext) {
        this.employeeRepository = employeeRepository;
        this.tenantContext = tenantContext;
    }

    @Cacheable(cacheNames = "dashboard", keyGenerator = "tenantCacheKeyGenerator")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DashboardResponse getDashboardSummary() {
        String tenantId = tenantContext.currentTenantId();
        var groups = employeeRepository.summarizeWorkforce(tenantId);
        if (groups == null) groups = List.of();
        long total = 0, exited = 0, retained = 0, ageCount = 0, ageTotal = 0, incomeCount = 0, incomeTotal = 0;
        Map<String, Long> departments = new TreeMap<>();
        List<DashboardResponse.DepartmentSummary> summaries = new ArrayList<>();
        for (var group : groups) {
            if (group == null) continue;
            long count = zero(group.getEmployeeCount());
            long yes = zero(group.getAttritionCount());
            long no = zero(group.getRetainedCount());
            total += count;
            exited += yes;
            retained += no;
            ageCount += group.getAgeCount();
            ageTotal += zero(group.getAgeTotal());
            incomeCount += group.getIncomeCount();
            incomeTotal += zero(group.getIncomeTotal());
            if (group.getDepartment() != null) departments.put(group.getDepartment(), count);
            summaries.add(new DashboardResponse.DepartmentSummary(group.getDepartment(), count, yes + no, yes,
                    ratio(yes, yes + no), ratio(zero(group.getAgeTotal()), group.getAgeCount()),
                    ratio(zero(group.getIncomeTotal()), group.getIncomeCount())));
        }
        summaries.sort(java.util.Comparator.comparing(DashboardResponse.DepartmentSummary::getDepartment,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        Map<String, Long> attrition = new TreeMap<>();
        attrition.put("Yes", exited);
        attrition.put("No", retained);
        attrition.put("Unlabeled", total - exited - retained);
        Map<String, Long> performance = new TreeMap<>();
        long performanceUnlabeled = 0;
        var performanceGroups = employeeRepository.summarizePerformance(tenantId);
        if (performanceGroups != null) for (var group : performanceGroups) {
            if (group == null) continue;
            if (group.getRating() == null) performanceUnlabeled += zero(group.getEmployeeCount());
            else performance.put(String.valueOf(group.getRating()), zero(group.getEmployeeCount()));
        }
        if (performanceUnlabeled > 0) performance.put("Unlabeled", performanceUnlabeled);
        return new DashboardResponse(total, departments, summaries, attrition, performance, exited + retained,
                exited, ratio(exited, exited + retained), ratio(ageTotal, ageCount), ratio(incomeTotal, incomeCount),
                new DashboardResponse.SnapshotMetadata("employee_records", false, true,
                        List.of("Current tenant employee-record snapshot; not a time-series or annual turnover rate.",
                                "Attrition rate is a 0-1 fraction of trimmed, case-insensitive Yes/No labels; other labels are Unlabeled.",
                                "Averages exclude null values; performance categories are stored ratings without inferred labels.",
                                "Missing departments use null in departmentSummaries and are omitted from departmentStatistics.",
                                "Monthly income currency is not recorded. This is decision support, not an employment decision."),
                        ageCount, incomeCount, performanceUnlabeled));
    }

    private static long zero(Long value) { return value == null ? 0 : value; }

    private static Double ratio(long numerator, long denominator) {
        return denominator == 0 ? null : (double) numerator / denominator;
    }
}
