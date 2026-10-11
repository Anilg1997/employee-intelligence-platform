package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.employeeintelligence.api.service.AuditService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuditService auditService;

    public DashboardController(DashboardService dashboardService, AuditService auditService) {
        this.dashboardService = dashboardService;
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public DashboardResponse getSummary() {
        var response = dashboardService.getDashboardSummary(); auditService.record("RISK_SUMMARY", "dashboard", "summary", "SUCCESS", "summary viewed"); return response;
    }
}
