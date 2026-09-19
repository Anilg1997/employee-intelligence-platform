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

public void deleteEmployee(Long id) {

    if (!employeeRepository.existsById(id)) {
        throw new RuntimeException("Employee not found with id: " + id);
    }

    employeeRepository.deleteById(id);
}
}