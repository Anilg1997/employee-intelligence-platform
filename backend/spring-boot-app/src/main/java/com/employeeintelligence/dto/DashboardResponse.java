package com.employeeintelligence.api.dto;

import java.util.Map;

public class DashboardResponse {

    private long totalEmployees;
    private Map<String, Long> departmentStatistics;

    public DashboardResponse(
            long totalEmployees,
            Map<String, Long> departmentStatistics) {

        this.totalEmployees = totalEmployees;
        this.departmentStatistics = departmentStatistics;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public Map<String, Long> getDepartmentStatistics() {
        return departmentStatistics;
    }
}