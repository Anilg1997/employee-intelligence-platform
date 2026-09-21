package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import org.springframework.web.bind.annotation.*;
import com.employeeintelligence.api.dto.AttritionRiskResponse;
@RestController
@RequestMapping("/api/ai/employees")
public class EmployeeAiController {

    private final EmployeeAiService employeeAiService;

    public EmployeeAiController(EmployeeAiService employeeAiService) {
        this.employeeAiService = employeeAiService;
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
}