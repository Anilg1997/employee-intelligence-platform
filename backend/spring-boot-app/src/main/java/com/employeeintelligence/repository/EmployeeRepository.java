package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByEmployeeNumber(Integer employeeNumber);
}