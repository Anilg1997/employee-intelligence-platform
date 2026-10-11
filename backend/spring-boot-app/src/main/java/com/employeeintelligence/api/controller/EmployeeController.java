package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.EmployeeRequest;
import com.employeeintelligence.api.dto.EmployeeResponse;
import com.employeeintelligence.api.dto.EmployeePageResponse;
import com.employeeintelligence.api.mapper.EmployeeMapper;
import com.employeeintelligence.api.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.employeeintelligence.api.service.AuditService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuditService auditService;

    public EmployeeController(EmployeeService employeeService, AuditService auditService) {
        this.employeeService = employeeService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<EmployeeResponse> getEmployees() {
        return employeeService.getEmployees().stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @GetMapping("/search")
    public EmployeePageResponse searchEmployees(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        return employeeService.searchEmployees(query, department, page, size, sortBy, sortDirection);
    }

    @PostMapping
    public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeRequest employee) {
        var saved = employeeService.createEmployee(EmployeeMapper.toEntity(employee));
        auditService.record("EMPLOYEE_CREATE", "employee", saved.getId(), "SUCCESS", "created employee record");
        return EmployeeMapper.toResponse(saved);
    }
    @GetMapping("/{id}")
public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {

    return employeeService.getEmployeeById(id)
            .map(EmployeeMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
@PutMapping("/{id}")
public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody EmployeeRequest employee) {
     var updated = employeeService.updateEmployee(id, EmployeeMapper.toEntity(employee));
     auditService.record("EMPLOYEE_UPDATE", "employee", id, "SUCCESS", "updated employee record");
     return ResponseEntity.ok(EmployeeMapper.toResponse(updated));
}
 @DeleteMapping("/{id}")
 @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN')")
 public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
     employeeService.deleteEmployee(id);
     auditService.record("EMPLOYEE_DELETE", "employee", id, "SUCCESS", "deleted employee record");
    return ResponseEntity.noContent().build();
 }
}
