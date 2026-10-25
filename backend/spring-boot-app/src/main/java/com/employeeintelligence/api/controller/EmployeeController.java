package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.EmployeeRequest;
import com.employeeintelligence.api.dto.EmployeeResponse;
import com.employeeintelligence.api.dto.EmployeePageResponse;
import com.employeeintelligence.api.mapper.EmployeeMapper;
import com.employeeintelligence.api.service.EmployeeService;
import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.employeeintelligence.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.employeeintelligence.api.service.AuditService;
import com.employeeintelligence.api.service.Employee360Service;
import com.employeeintelligence.api.dto.Employee360Response;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuditService auditService;
    private final Employee360Service employee360Service;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public EmployeeController(EmployeeService employeeService, AuditService auditService,
                              Employee360Service employee360Service, EmployeeRepository employeeRepository, TenantContext tenantContext) {
        this.employeeService = employeeService;
        this.auditService = auditService;
        this.employee360Service = employee360Service;
        this.employeeRepository = employeeRepository;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public List<EmployeeResponse> getEmployees() {
        return employeeService.getEmployees().stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @GetMapping("/search")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
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
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeRequest employee) {
        var saved = employeeService.createEmployee(EmployeeMapper.toEntity(employee));
        auditService.record("EMPLOYEE_CREATE", "employee", saved.getId(), "SUCCESS", "created employee record");
        return EmployeeMapper.toResponse(saved);
    }
    @GetMapping("/{id}")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return EmployeeMapper.toResponse(employeeService.getEmployeeById(id));
    }

    @GetMapping("/{id}/360")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public Employee360Response getEmployee360(@PathVariable Long id,
                                              @RequestParam(defaultValue = "25") int timelineSize) {
        return employee360Service.getEmployee360(id, timelineSize);
    }
    @PutMapping("/{id}")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest employee) {
        var updated = employeeService.updateEmployee(id, EmployeeMapper.toEntity(employee));
        auditService.record("EMPLOYEE_UPDATE", "employee", id, "SUCCESS", "updated employee record");
        return ResponseEntity.ok(EmployeeMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        auditService.record("EMPLOYEE_DELETE", "employee", id, "SUCCESS", "deleted employee record");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployeeStatus(
            @PathVariable Long id,
            @RequestParam String employmentStatus) {
        Employee existingEmployee = employeeRepository.findByIdAndTenantId(id, tenantContext.currentTenantId())
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        existingEmployee.setEmploymentStatus(employmentStatus);
        Employee updated = employeeRepository.save(existingEmployee);
        auditService.record("EMPLOYEE_STATUS_UPDATE", "employee", id, "SUCCESS", "updated employment status to: " + employmentStatus);
        return ResponseEntity.ok(EmployeeMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/hire-date")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployeeHireDate(
            @PathVariable Long id,
            @RequestParam java.time.LocalDate hireDate) {
        Employee existingEmployee = employeeRepository.findByIdAndTenantId(id, tenantContext.currentTenantId())
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        existingEmployee.setHireDate(hireDate);
        Employee updated = employeeRepository.save(existingEmployee);
        auditService.record("EMPLOYEE_HIRE_DATE_UPDATE", "employee", id, "SUCCESS", "updated hire date to: " + hireDate);
        return ResponseEntity.ok(EmployeeMapper.toResponse(updated));
    }
}
