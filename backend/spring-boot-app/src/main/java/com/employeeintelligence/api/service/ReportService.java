package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.*;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.employeeintelligence.api.model.Report;
import com.employeeintelligence.api.repository.*;
import com.employeeintelligence.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class ReportService {
    private static final List<String> LIMITATIONS = List.of(
            "Deterministic report generated from stored employee and dashboard aggregates; no facts are invented.",
            "AI assistance is disabled for this report; content is not an employment decision and requires human review.",
            "Current records are a snapshot, not a time series; missing values are not inferred.");
    private final ReportRepository reports;
    private final EmployeeRepository employees;
    private final DashboardService dashboard;
    private final TenantContext tenantContext;
    private final ObjectMapper mapper;

    public ReportService(ReportRepository reports, EmployeeRepository employees, DashboardService dashboard,
                         TenantContext tenantContext, ObjectMapper mapper) {
        this.reports = reports; this.employees = employees; this.dashboard = dashboard;
        this.tenantContext = tenantContext; this.mapper = mapper;
    }

    @Transactional
    public ReportResponse generate(ReportRequest request) {
        String type = request.reportType().toUpperCase(Locale.ROOT);
        validateTarget(type, request);
        String tenant = tenantContext.currentTenantId();
        Report report = new Report();
        report.setTenantId(tenant); report.setReportType(type); report.setEmployeeId(request.employeeId());
        report.setDepartment(normalize(request.department())); report.setStatus("GENERATED");
        report.setAiAssisted(false); report.setHumanReviewState("PENDING"); report.setCreatedAt(Instant.now());
        report.setRequester(AuthenticatedUser.current().subject());
        try {
            report.setContent(mapper.writeValueAsString(content(type, request, tenant)));
            report.setLimitations(mapper.writeValueAsString(LIMITATIONS));
        } catch (Exception e) { throw new IllegalStateException("Could not serialize report content", e); }
        return ReportResponse.from(reports.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> list(int size) {
        int bounded = Math.min(Math.max(size, 1), 100);
        return reports.findAllByTenantId(tenantContext.currentTenantId(), PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(ReportResponse::from).getContent();
    }

    @Transactional(readOnly = true)
    public ReportResponse get(Long id) {
        return ReportResponse.from(reports.findByIdAndTenantId(id, tenantContext.currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found")));
    }

    private Map<String, Object> content(String type, ReportRequest request, String tenant) {
        Map<String, Object> result = new TreeMap<>();
        result.put("reportType", type); result.put("source", "employee_records_and_dashboard");
        if ("EMPLOYEE".equals(type)) {
            var e = employees.findByIdAndTenantId(request.employeeId(), tenant)
                    .orElseThrow(() -> new EmployeeNotFoundException(request.employeeId()));
            Map<String, Object> facts = new TreeMap<>();
            facts.put("id", e.getId()); facts.put("name", e.getName()); facts.put("department", e.getDepartment());
            facts.put("jobRole", e.getJobRole()); facts.put("age", e.getAge()); facts.put("attrition", e.getAttrition());
            facts.put("performanceRating", e.getPerformanceRating()); facts.put("yearsAtCompany", e.getYearsAtCompany());
            result.put("employee", facts);
        } else {
            var d = dashboard.getDashboardSummary();
            result.put("dashboard", "DEPARTMENT".equals(type) ? d.getDepartmentSummaries().stream()
                    .filter(s -> request.department().equalsIgnoreCase(s.getDepartment())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Department not found")) : d);
        }
        return result;
    }

    private static void validateTarget(String type, ReportRequest r) {
        if ("EMPLOYEE".equals(type) && (r.employeeId() == null || r.department() != null)) throw new IllegalArgumentException("EMPLOYEE reports require employeeId only");
        if ("DEPARTMENT".equals(type) && (r.employeeId() != null || normalize(r.department()) == null)) throw new IllegalArgumentException("DEPARTMENT reports require department only");
        if ("WORKFORCE".equals(type) && (r.employeeId() != null || normalize(r.department()) != null)) throw new IllegalArgumentException("WORKFORCE reports do not accept a target");
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
