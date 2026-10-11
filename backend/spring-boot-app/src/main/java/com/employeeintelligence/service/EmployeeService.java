package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.employeeintelligence.api.dto.EmployeePageResponse;
import com.employeeintelligence.api.mapper.EmployeeMapper;
import com.employeeintelligence.security.TenantContext;

@Service
public class EmployeeService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "id", "id", "name", "name", "department", "department",
            "jobRole", "jobRole", "age", "age", "employeeNumber", "employeeNumber");

    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public EmployeeService(EmployeeRepository employeeRepository, TenantContext tenantContext) {
        this.employeeRepository = employeeRepository; this.tenantContext = tenantContext;
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAllByTenantId(tenantContext.currentTenantId());
    }

    public EmployeePageResponse searchEmployees(String query, String department,
                                                  String page, String size,
                                                  String sortBy, String sortDirection) {
        int normalizedPage = parseNonNegative(page, DEFAULT_PAGE);
        int normalizedSize = Math.min(Math.max(parseNonNegative(size, DEFAULT_SIZE), 1), MAX_SIZE);
        String property = sortBy == null ? "name" : SORTABLE_FIELDS.getOrDefault(sortBy, "name");
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize,
                Sort.by(direction, property));
        Page<Employee> result = employeeRepository.search(tenantContext.currentTenantId(), normalize(query), normalize(department), pageable);
        return new EmployeePageResponse(result.getContent().stream().map(EmployeeMapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static int parseNonNegative(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public Employee createEmployee(Employee employee) {
        employee.setTenantId(tenantContext.currentTenantId());
        return employeeRepository.save(employee);
    }
    public Optional<Employee> getEmployeeById(Long id) {
    return employeeRepository.findByIdAndTenantId(id, tenantContext.currentTenantId());
}
public Employee updateEmployee(Long id, Employee updatedEmployee) {

    Employee existingEmployee = employeeRepository.findByIdAndTenantId(id, tenantContext.currentTenantId())
            .orElseThrow(() -> new EmployeeNotFoundException(id));

    existingEmployee.setName(updatedEmployee.getName());
    existingEmployee.setDepartment(updatedEmployee.getDepartment());
    existingEmployee.setJobRole(updatedEmployee.getJobRole());
    existingEmployee.setAge(updatedEmployee.getAge());
    existingEmployee.setAttrition(updatedEmployee.getAttrition());
    existingEmployee.setBusinessTravel(updatedEmployee.getBusinessTravel());
    existingEmployee.setDailyRate(updatedEmployee.getDailyRate());
    existingEmployee.setDistanceFromHome(updatedEmployee.getDistanceFromHome());
    existingEmployee.setEducation(updatedEmployee.getEducation());
    existingEmployee.setEducationField(updatedEmployee.getEducationField());
    existingEmployee.setEmployeeCount(updatedEmployee.getEmployeeCount());
    existingEmployee.setEmployeeNumber(updatedEmployee.getEmployeeNumber());
    existingEmployee.setEnvironmentSatisfaction(updatedEmployee.getEnvironmentSatisfaction());
    existingEmployee.setGender(updatedEmployee.getGender());
    existingEmployee.setHourlyRate(updatedEmployee.getHourlyRate());
    existingEmployee.setJobInvolvement(updatedEmployee.getJobInvolvement());
    existingEmployee.setJobLevel(updatedEmployee.getJobLevel());
    existingEmployee.setJobSatisfaction(updatedEmployee.getJobSatisfaction());
    existingEmployee.setMaritalStatus(updatedEmployee.getMaritalStatus());
    existingEmployee.setMonthlyIncome(updatedEmployee.getMonthlyIncome());
    existingEmployee.setMonthlyRate(updatedEmployee.getMonthlyRate());
    existingEmployee.setNumCompaniesWorked(updatedEmployee.getNumCompaniesWorked());
    existingEmployee.setOver18(updatedEmployee.getOver18());
    existingEmployee.setOverTime(updatedEmployee.getOverTime());
    existingEmployee.setPercentSalaryHike(updatedEmployee.getPercentSalaryHike());
    existingEmployee.setPerformanceRating(updatedEmployee.getPerformanceRating());
    existingEmployee.setRelationshipSatisfaction(updatedEmployee.getRelationshipSatisfaction());
    existingEmployee.setStandardHours(updatedEmployee.getStandardHours());
    existingEmployee.setStockOptionLevel(updatedEmployee.getStockOptionLevel());
    existingEmployee.setTotalWorkingYears(updatedEmployee.getTotalWorkingYears());
    existingEmployee.setTrainingTimesLastYear(updatedEmployee.getTrainingTimesLastYear());
    existingEmployee.setWorkLifeBalance(updatedEmployee.getWorkLifeBalance());
    existingEmployee.setYearsAtCompany(updatedEmployee.getYearsAtCompany());
    existingEmployee.setYearsInCurrentRole(updatedEmployee.getYearsInCurrentRole());
    existingEmployee.setYearsSinceLastPromotion(updatedEmployee.getYearsSinceLastPromotion());
    existingEmployee.setYearsWithCurrManager(updatedEmployee.getYearsWithCurrManager());

    return employeeRepository.save(existingEmployee);
}

@org.springframework.transaction.annotation.Transactional
public void deleteEmployee(Long id) {

    if (!employeeRepository.existsByIdAndTenantId(id, tenantContext.currentTenantId())) {
        throw new EmployeeNotFoundException(id);
    }

    employeeRepository.deleteByIdAndTenantId(id, tenantContext.currentTenantId());
}
}
