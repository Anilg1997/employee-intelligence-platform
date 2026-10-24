package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.AuditEventResponse;
import com.employeeintelligence.api.dto.Employee360Response;
import com.employeeintelligence.api.mapper.EmployeeMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class Employee360Service {
    static final int DEFAULT_TIMELINE_SIZE = 25;
    static final int MAX_TIMELINE_SIZE = 100;

    private final EmployeeService employeeService;
    private final AuditService auditService;
    private final RiskSummaryService riskSummaryService;

    public Employee360Service(EmployeeService employeeService, AuditService auditService,
                              RiskSummaryService riskSummaryService) {
        this.employeeService = employeeService;
        this.auditService = auditService;
        this.riskSummaryService = riskSummaryService;
    }

    public Employee360Response getEmployee360(Long employeeId, int timelineSize) {
        var employee = employeeService.getEmployeeById(employeeId);
        int boundedSize = Math.min(Math.max(timelineSize, 1), MAX_TIMELINE_SIZE);
        var timeline = auditService.findForEmployee(employeeId,
                        PageRequest.of(0, boundedSize, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent().stream().map(AuditEventResponse::from).toList();
        return new Employee360Response(EmployeeMapper.toResponse(employee), timeline,
                riskSummaryService.summarize(employeeId));
    }
}
