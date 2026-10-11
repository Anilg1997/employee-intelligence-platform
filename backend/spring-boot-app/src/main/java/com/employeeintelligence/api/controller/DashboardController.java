package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public DashboardResponse getSummary() {
        var response = dashboardService.getDashboardSummary(); auditService.record("RISK_SUMMARY", "dashboard", "summary", "SUCCESS", "summary viewed"); return response;
    }
}
