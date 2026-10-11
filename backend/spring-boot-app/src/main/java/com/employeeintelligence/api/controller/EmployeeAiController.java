package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.employeeintelligence.api.dto.AttritionRiskResponse;
import com.employeeintelligence.api.dto.RiskSummaryResponse;
import com.employeeintelligence.api.service.RiskSummaryService;
import com.employeeintelligence.api.service.AuditService;
@RestController
@RequestMapping("/api/ai/employees")
public class EmployeeAiController {

    private final EmployeeAiService employeeAiService;
    private final RiskSummaryService riskSummaryService;
    private final AuditService auditService;

    public EmployeeAiController(EmployeeAiService employeeAiService, RiskSummaryService riskSummaryService, AuditService auditService) {
        this.employeeAiService = employeeAiService;
        this.riskSummaryService = riskSummaryService;
        this.auditService = auditService;
    }

    @GetMapping("/{employeeId}/prediction")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public MlPredictionResponse predictEmployee(
            @PathVariable Long employeeId) {

        return employeeAiService.predictEmployee(employeeId);
    }
    @GetMapping("/{employeeId}/risk")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
public AttritionRiskResponse assessRisk(
        @PathVariable Long employeeId) {

    return employeeAiService.assessRisk(employeeId);
    }

    @GetMapping("/{employeeId}/risk-summary")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public RiskSummaryResponse riskSummary(@PathVariable Long employeeId) { var response = riskSummaryService.summarize(employeeId); auditService.record("RISK_SUMMARY", "employee", employeeId, "SUCCESS", "risk summary viewed"); return response; }
}
