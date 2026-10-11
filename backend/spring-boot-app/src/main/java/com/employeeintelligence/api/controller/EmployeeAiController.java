package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import org.springframework.web.bind.annotation.*;
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
    public MlPredictionResponse predictEmployee(
            @PathVariable Long employeeId) {

        return employeeAiService.predictEmployee(employeeId);
    }
    @GetMapping("/{employeeId}/risk")
public AttritionRiskResponse assessRisk(
        @PathVariable Long employeeId) {

    return employeeAiService.assessRisk(employeeId);
    }

    @GetMapping("/{employeeId}/risk-summary")
    public RiskSummaryResponse riskSummary(@PathVariable Long employeeId) { var response = riskSummaryService.summarize(employeeId); auditService.record("RISK_SUMMARY", "employee", employeeId, "SUCCESS", "risk summary viewed"); return response; }
}
