package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}