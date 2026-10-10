package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import org.springframework.web.bind.annotation.*;
import com.employeeintelligence.api.dto.AttritionRiskResponse;
import com.employeeintelligence.api.dto.RiskSummaryResponse;
import com.employeeintelligence.api.service.RiskSummaryService;
@RestController
@RequestMapping("/api/ai/employees")
public class EmployeeAiController {

    private final EmployeeAiService employeeAiService;
    private final RiskSummaryService riskSummaryService;

    public EmployeeAiController(EmployeeAiService employeeAiService, RiskSummaryService riskSummaryService) {
        this.employeeAiService = employeeAiService;
        this.riskSummaryService = riskSummaryService;
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
    public RiskSummaryResponse riskSummary(@PathVariable Long employeeId) { return riskSummaryService.summarize(employeeId); }
}
