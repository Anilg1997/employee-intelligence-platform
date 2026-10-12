package com.employeeintelligence.api.dto;

import java.util.Map;

public class DashboardResponse {

    private long totalEmployees;
    private Map<String, Long> departmentStatistics;
    private long attritionLabeledEmployees;
    private long attritionCount;
    private Double attritionRate;
    private Double averageAge;
    private Double averageMonthlyIncome;

    public DashboardResponse(
            long totalEmployees,
            Map<String, Long> departmentStatistics,
            long attritionLabeledEmployees, long attritionCount, Double attritionRate,
            Double averageAge, Double averageMonthlyIncome) {

        this.totalEmployees = totalEmployees;
        this.departmentStatistics = departmentStatistics;
        this.attritionLabeledEmployees = attritionLabeledEmployees;
        this.attritionCount = attritionCount;
        this.attritionRate = attritionRate;
        this.averageAge = averageAge;
        this.averageMonthlyIncome = averageMonthlyIncome;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public Map<String, Long> getDepartmentStatistics() {
        return departmentStatistics;
    }

    public long getAttritionLabeledEmployees() { return attritionLabeledEmployees; }
    public long getAttritionCount() { return attritionCount; }
    public Double getAttritionRate() { return attritionRate; }
    public Double getAverageAge() { return averageAge; }
    public Double getAverageMonthlyIncome() { return averageMonthlyIncome; }
}
