package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    public Optional<Employee> getEmployeeById(Long id) {
    return employeeRepository.findById(id);
}
public Employee updateEmployee(Long id, Employee updatedEmployee) {

    Employee existingEmployee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

    existingEmployee.setName(updatedEmployee.getName());
    existingEmployee.setDepartment(updatedEmployee.getDepartment());
    existingEmployee.setJobRole(updatedEmployee.getJobRole());
    existingEmployee.setAge(updatedEmployee.getAge());

    return employeeRepository.save(existingEmployee);
}

public void deleteEmployee(Long id) {

    if (!employeeRepository.existsById(id)) {
        throw new RuntimeException("Employee not found with id: " + id);
    }

    employeeRepository.deleteById(id);
}
}