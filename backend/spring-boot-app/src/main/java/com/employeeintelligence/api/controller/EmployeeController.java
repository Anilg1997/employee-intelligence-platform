package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.EmployeeRequest;
import com.employeeintelligence.api.dto.EmployeeResponse;
import com.employeeintelligence.api.mapper.EmployeeMapper;
import com.employeeintelligence.api.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getEmployees() {
        return employeeService.getEmployees().stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @PostMapping
    public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeRequest employee) {
        return EmployeeMapper.toResponse(employeeService.createEmployee(EmployeeMapper.toEntity(employee)));
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
    return ResponseEntity.ok(EmployeeMapper.toResponse(
            employeeService.updateEmployee(id, EmployeeMapper.toEntity(employee))));
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
    employeeService.deleteEmployee(id);
    return ResponseEntity.noContent().build();
}
}
