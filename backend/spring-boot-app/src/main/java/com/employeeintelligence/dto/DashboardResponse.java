package com.employeeintelligence.api.dto;

import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.TreeMap;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DashboardResponse {

    private long totalEmployees;
    private Map<String, Long> departmentStatistics;
    private long attritionLabeledEmployees;
    private long attritionCount;
    private Double attritionRate;
    private Double averageAge;
    private Double averageMonthlyIncome;
    private List<DepartmentSummary> departmentSummaries;
    private Map<String, Long> attritionDistribution;
    private Map<String, Long> performanceDistribution;
    private SnapshotMetadata snapshotMetadata;

    public DashboardResponse(
            long totalEmployees,
            Map<String, Long> departmentStatistics,
            long attritionLabeledEmployees, long attritionCount, Double attritionRate,
            Double averageAge, Double averageMonthlyIncome) {

        this.totalEmployees = totalEmployees;
        this.departmentStatistics = immutableSortedMap(departmentStatistics);
        this.attritionLabeledEmployees = attritionLabeledEmployees;
        this.attritionCount = attritionCount;
        this.attritionRate = attritionRate;
        this.averageAge = averageAge;
        this.averageMonthlyIncome = averageMonthlyIncome;
        this.departmentSummaries = this.departmentStatistics.entrySet().stream()
                .map(e -> new DepartmentSummary(e.getKey(), e.getValue())).toList();
        this.attritionDistribution = Map.of();
        this.performanceDistribution = Map.of();
        this.snapshotMetadata = new SnapshotMetadata("employee_records", false, true,
                List.of("Aggregates reflect stored employee fields only.",
                        "This response is decision support and is not an employment decision."));
    }

    @JsonCreator
    public DashboardResponse(@JsonProperty("totalEmployees") long totalEmployees,
            @JsonProperty("departmentStatistics") Map<String, Long> departmentStatistics,
            @JsonProperty("departmentSummaries") List<DepartmentSummary> departmentSummaries,
            @JsonProperty("attritionDistribution") Map<String, Long> attritionDistribution,
            @JsonProperty("performanceDistribution") Map<String, Long> performanceDistribution,
            @JsonProperty("attritionLabeledEmployees") long attritionLabeledEmployees,
            @JsonProperty("attritionCount") long attritionCount,
            @JsonProperty("attritionRate") Double attritionRate,
            @JsonProperty("averageAge") Double averageAge,
            @JsonProperty("averageMonthlyIncome") Double averageMonthlyIncome,
            @JsonProperty("snapshotMetadata") SnapshotMetadata snapshotMetadata) {
        this(totalEmployees, departmentStatistics, attritionLabeledEmployees, attritionCount,
                attritionRate, averageAge, averageMonthlyIncome);
        this.departmentSummaries = new ArrayList<>(departmentSummaries == null ? List.of() : departmentSummaries);
        this.attritionDistribution = immutableSortedMap(attritionDistribution);
        this.performanceDistribution = immutableSortedMap(performanceDistribution);
        this.snapshotMetadata = snapshotMetadata;
    }

    public DashboardResponse(long totalEmployees, Map<String, Long> departmentStatistics,
            List<DepartmentSummary> departmentSummaries, Map<String, Long> attritionDistribution,
            Map<String, Long> performanceDistribution, long attritionLabeledEmployees,
            long attritionCount, Double attritionRate, Double averageAge, Double averageMonthlyIncome,
            SnapshotMetadata snapshotMetadata, long ageLabeledEmployees, long incomeLabeledEmployees,
            long performanceUnlabeledEmployees) {
        this(totalEmployees, departmentStatistics, departmentSummaries, attritionDistribution,
                performanceDistribution, attritionLabeledEmployees, attritionCount, attritionRate,
                averageAge, averageMonthlyIncome,
                new SnapshotMetadata(snapshotMetadata.getSource(), snapshotMetadata.isTruncated(),
                        snapshotMetadata.isDecisionSupportOnly(), snapshotMetadata.getLimitations(),
                        ageLabeledEmployees, incomeLabeledEmployees, performanceUnlabeledEmployees));
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
    public List<DepartmentSummary> getDepartmentSummaries() { return departmentSummaries; }
    public Map<String, Long> getAttritionDistribution() { return attritionDistribution; }
    public Map<String, Long> getPerformanceDistribution() { return performanceDistribution; }
    public SnapshotMetadata getSnapshotMetadata() { return snapshotMetadata; }

    private static Map<String, Long> immutableSortedMap(Map<String, Long> values) {
        return new TreeMap<>(values == null ? Map.of() : values);
    }

    public static class DepartmentSummary {
        private final String department;
        private final long employeeCount;
        private final long attritionLabeledEmployees;
        private final long attritionCount;
        private final Double attritionRate;
        private final Double averageAge;
        private final Double averageMonthlyIncome;
        public DepartmentSummary(String department, long employeeCount) {
            this(department, employeeCount, 0, 0, null, null, null);
        }
        @JsonCreator
        public DepartmentSummary(@JsonProperty("department") String department,
                @JsonProperty("employeeCount") long employeeCount,
                @JsonProperty("attritionLabeledEmployees") long attritionLabeledEmployees,
                @JsonProperty("attritionCount") long attritionCount,
                @JsonProperty("attritionRate") Double attritionRate,
                @JsonProperty("averageAge") Double averageAge,
                @JsonProperty("averageMonthlyIncome") Double averageMonthlyIncome) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.attritionLabeledEmployees = attritionLabeledEmployees;
            this.attritionCount = attritionCount;
            this.attritionRate = attritionRate;
            this.averageAge = averageAge;
            this.averageMonthlyIncome = averageMonthlyIncome;
        }
        public String getDepartment() { return department; }
        public long getEmployeeCount() { return employeeCount; }
        public long getAttritionLabeledEmployees() { return attritionLabeledEmployees; }
        public long getAttritionCount() { return attritionCount; }
        public Double getAttritionRate() { return attritionRate; }
        public Double getAverageAge() { return averageAge; }
        public Double getAverageMonthlyIncome() { return averageMonthlyIncome; }
    }

    public static class SnapshotMetadata {
        private final String source;
        private final boolean truncated;
        private final boolean decisionSupportOnly;
        private final List<String> limitations;
        private final long ageLabeledEmployees;
        private final long incomeLabeledEmployees;
        private final long performanceUnlabeledEmployees;
        public SnapshotMetadata(String source, boolean truncated, boolean decisionSupportOnly, List<String> limitations) {
            this(source, truncated, decisionSupportOnly, limitations, 0, 0, 0);
        }
        @JsonCreator
        public SnapshotMetadata(@JsonProperty("source") String source,
                @JsonProperty("truncated") boolean truncated,
                @JsonProperty("decisionSupportOnly") boolean decisionSupportOnly,
                @JsonProperty("limitations") List<String> limitations,
                @JsonProperty("ageLabeledEmployees") long ageLabeledEmployees,
                @JsonProperty("incomeLabeledEmployees") long incomeLabeledEmployees,
                @JsonProperty("performanceUnlabeledEmployees") long performanceUnlabeledEmployees) {
            this.source = source; this.truncated = truncated; this.decisionSupportOnly = decisionSupportOnly;
            this.limitations = new ArrayList<>(limitations == null ? List.of() : limitations);
            this.ageLabeledEmployees = ageLabeledEmployees;
            this.incomeLabeledEmployees = incomeLabeledEmployees;
            this.performanceUnlabeledEmployees = performanceUnlabeledEmployees;
        }
        public String getSource() { return source; }
        public boolean isTruncated() { return truncated; }
        public boolean isDecisionSupportOnly() { return decisionSupportOnly; }
        public List<String> getLimitations() { return limitations; }
        public long getAgeLabeledEmployees() { return ageLabeledEmployees; }
        public long getIncomeLabeledEmployees() { return incomeLabeledEmployees; }
        public long getPerformanceUnlabeledEmployees() { return performanceUnlabeledEmployees; }
    }
}
