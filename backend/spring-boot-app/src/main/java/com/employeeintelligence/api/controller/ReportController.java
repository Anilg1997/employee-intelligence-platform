package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.*;
import com.employeeintelligence.api.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service; private final AuditService audit;
    public ReportController(ReportService service, AuditService audit) { this.service = service; this.audit = audit; }
    @GetMapping
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public List<ReportResponse> list(@RequestParam(defaultValue = "50") int size) { return service.list(size); }
    @GetMapping("/{id}")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public ReportResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public ReportResponse generate(@Valid @RequestBody ReportRequest request) {
        var response = service.generate(request);
        audit.record("REPORT_GENERATE", "report", response.id(), "SUCCESS", "generated deterministic report");
        return response;
    }
}
