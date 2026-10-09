package com.employeeintelligence.api.mapper;

import com.employeeintelligence.api.dto.EmployeeRequest;
import com.employeeintelligence.api.dto.EmployeeResponse;
import com.employeeintelligence.api.model.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(EmployeeRequest request) {
        // Identity is generated on create and selected by the URL on update.
        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setDepartment(request.department());
        employee.setJobRole(request.jobRole());
        employee.setAge(request.age());
        employee.setAttrition(request.attrition());
        employee.setBusinessTravel(request.businessTravel());
        employee.setDailyRate(request.dailyRate());
        employee.setDistanceFromHome(request.distanceFromHome());
        employee.setEducation(request.education());
        employee.setEducationField(request.educationField());
        employee.setEmployeeCount(request.employeeCount());
        employee.setEmployeeNumber(request.employeeNumber());
        employee.setEnvironmentSatisfaction(request.environmentSatisfaction());
        employee.setGender(request.gender());
        employee.setHourlyRate(request.hourlyRate());
        employee.setJobInvolvement(request.jobInvolvement());
        employee.setJobLevel(request.jobLevel());
        employee.setJobSatisfaction(request.jobSatisfaction());
        employee.setMaritalStatus(request.maritalStatus());
        employee.setMonthlyIncome(request.monthlyIncome());
        employee.setMonthlyRate(request.monthlyRate());
        employee.setNumCompaniesWorked(request.numCompaniesWorked());
        employee.setOver18(request.over18());
        employee.setOverTime(request.overTime());
        employee.setPercentSalaryHike(request.percentSalaryHike());
        employee.setPerformanceRating(request.performanceRating());
        employee.setRelationshipSatisfaction(request.relationshipSatisfaction());
        employee.setStandardHours(request.standardHours());
        employee.setStockOptionLevel(request.stockOptionLevel());
        employee.setTotalWorkingYears(request.totalWorkingYears());
        employee.setTrainingTimesLastYear(request.trainingTimesLastYear());
        employee.setWorkLifeBalance(request.workLifeBalance());
        employee.setYearsAtCompany(request.yearsAtCompany());
        employee.setYearsInCurrentRole(request.yearsInCurrentRole());
        employee.setYearsSinceLastPromotion(request.yearsSinceLastPromotion());
        employee.setYearsWithCurrManager(request.yearsWithCurrManager());
        return employee;
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(), employee.getName(), employee.getDepartment(),
                employee.getJobRole(), employee.getAge(), employee.getAttrition(),
                employee.getBusinessTravel(), employee.getDailyRate(), employee.getDistanceFromHome(),
                employee.getEducation(), employee.getEducationField(), employee.getEmployeeCount(),
                employee.getEmployeeNumber(), employee.getEnvironmentSatisfaction(), employee.getGender(),
                employee.getHourlyRate(), employee.getJobInvolvement(), employee.getJobLevel(),
                employee.getJobSatisfaction(), employee.getMaritalStatus(), employee.getMonthlyIncome(),
                employee.getMonthlyRate(), employee.getNumCompaniesWorked(), employee.getOver18(),
                employee.getOverTime(), employee.getPercentSalaryHike(), employee.getPerformanceRating(),
                employee.getRelationshipSatisfaction(), employee.getStandardHours(), employee.getStockOptionLevel(),
                employee.getTotalWorkingYears(), employee.getTrainingTimesLastYear(), employee.getWorkLifeBalance(),
                employee.getYearsAtCompany(), employee.getYearsInCurrentRole(),
                employee.getYearsSinceLastPromotion(), employee.getYearsWithCurrManager());
    }
}
