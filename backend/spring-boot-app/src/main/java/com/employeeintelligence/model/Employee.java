package com.employeeintelligence.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "employeeNumber"))
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String department;

    private String jobRole;

    private Integer age;

    private String attrition;
    private String businessTravel;
    private Integer dailyRate;
    private Integer distanceFromHome;
    private Integer education;
    private String educationField;
    private Integer employeeCount;

    private Integer employeeNumber;

    private Integer environmentSatisfaction;
    private String gender;
    private Integer hourlyRate;
    private Integer jobInvolvement;
    private Integer jobLevel;
    private Integer jobSatisfaction;
    private String maritalStatus;
    private Integer monthlyIncome;
    private Integer monthlyRate;
    private Integer numCompaniesWorked;
    private String over18;
    private String overTime;
    private Integer percentSalaryHike;
    private Integer performanceRating;
    private Integer relationshipSatisfaction;
    private Integer standardHours;
    private Integer stockOptionLevel;
    private Integer totalWorkingYears;
    private Integer trainingTimesLastYear;
    private Integer workLifeBalance;
    private Integer yearsAtCompany;
    private Integer yearsInCurrentRole;
    private Integer yearsSinceLastPromotion;
    private Integer yearsWithCurrManager;

    public Employee() {
    }

    public Employee(String name, String department, String jobRole, Integer age) {
        this.name = name;
        this.department = department;
        this.jobRole = jobRole;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAttrition() { return attrition; }
    public void setAttrition(String attrition) { this.attrition = attrition; }
    public String getBusinessTravel() { return businessTravel; }
    public void setBusinessTravel(String businessTravel) { this.businessTravel = businessTravel; }
    public Integer getDailyRate() { return dailyRate; }
    public void setDailyRate(Integer dailyRate) { this.dailyRate = dailyRate; }
    public Integer getDistanceFromHome() { return distanceFromHome; }
    public void setDistanceFromHome(Integer distanceFromHome) { this.distanceFromHome = distanceFromHome; }
    public Integer getEducation() { return education; }
    public void setEducation(Integer education) { this.education = education; }
    public String getEducationField() { return educationField; }
    public void setEducationField(String educationField) { this.educationField = educationField; }
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
    public Integer getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(Integer employeeNumber) { this.employeeNumber = employeeNumber; }
    public Integer getEnvironmentSatisfaction() { return environmentSatisfaction; }
    public void setEnvironmentSatisfaction(Integer environmentSatisfaction) { this.environmentSatisfaction = environmentSatisfaction; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Integer hourlyRate) { this.hourlyRate = hourlyRate; }
    public Integer getJobInvolvement() { return jobInvolvement; }
    public void setJobInvolvement(Integer jobInvolvement) { this.jobInvolvement = jobInvolvement; }
    public Integer getJobLevel() { return jobLevel; }
    public void setJobLevel(Integer jobLevel) { this.jobLevel = jobLevel; }
    public Integer getJobSatisfaction() { return jobSatisfaction; }
    public void setJobSatisfaction(Integer jobSatisfaction) { this.jobSatisfaction = jobSatisfaction; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public Integer getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Integer monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public Integer getMonthlyRate() { return monthlyRate; }
    public void setMonthlyRate(Integer monthlyRate) { this.monthlyRate = monthlyRate; }
    public Integer getNumCompaniesWorked() { return numCompaniesWorked; }
    public void setNumCompaniesWorked(Integer numCompaniesWorked) { this.numCompaniesWorked = numCompaniesWorked; }
    public String getOver18() { return over18; }
    public void setOver18(String over18) { this.over18 = over18; }
    public String getOverTime() { return overTime; }
    public void setOverTime(String overTime) { this.overTime = overTime; }
    public Integer getPercentSalaryHike() { return percentSalaryHike; }
    public void setPercentSalaryHike(Integer percentSalaryHike) { this.percentSalaryHike = percentSalaryHike; }
    public Integer getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(Integer performanceRating) { this.performanceRating = performanceRating; }
    public Integer getRelationshipSatisfaction() { return relationshipSatisfaction; }
    public void setRelationshipSatisfaction(Integer relationshipSatisfaction) { this.relationshipSatisfaction = relationshipSatisfaction; }
    public Integer getStandardHours() { return standardHours; }
    public void setStandardHours(Integer standardHours) { this.standardHours = standardHours; }
    public Integer getStockOptionLevel() { return stockOptionLevel; }
    public void setStockOptionLevel(Integer stockOptionLevel) { this.stockOptionLevel = stockOptionLevel; }
    public Integer getTotalWorkingYears() { return totalWorkingYears; }
    public void setTotalWorkingYears(Integer totalWorkingYears) { this.totalWorkingYears = totalWorkingYears; }
    public Integer getTrainingTimesLastYear() { return trainingTimesLastYear; }
    public void setTrainingTimesLastYear(Integer trainingTimesLastYear) { this.trainingTimesLastYear = trainingTimesLastYear; }
    public Integer getWorkLifeBalance() { return workLifeBalance; }
    public void setWorkLifeBalance(Integer workLifeBalance) { this.workLifeBalance = workLifeBalance; }
    public Integer getYearsAtCompany() { return yearsAtCompany; }
    public void setYearsAtCompany(Integer yearsAtCompany) { this.yearsAtCompany = yearsAtCompany; }
    public Integer getYearsInCurrentRole() { return yearsInCurrentRole; }
    public void setYearsInCurrentRole(Integer yearsInCurrentRole) { this.yearsInCurrentRole = yearsInCurrentRole; }
    public Integer getYearsSinceLastPromotion() { return yearsSinceLastPromotion; }
    public void setYearsSinceLastPromotion(Integer yearsSinceLastPromotion) { this.yearsSinceLastPromotion = yearsSinceLastPromotion; }
    public Integer getYearsWithCurrManager() { return yearsWithCurrManager; }
    public void setYearsWithCurrManager(Integer yearsWithCurrManager) { this.yearsWithCurrManager = yearsWithCurrManager; }
}