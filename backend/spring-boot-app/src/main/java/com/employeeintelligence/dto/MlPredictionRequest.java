package com.employeeintelligence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MlPredictionRequest {

    @NotNull
    @Min(18)
    @Max(60)
    @JsonProperty("Age")
    private Integer age;

    @NotBlank
    @JsonProperty("BusinessTravel")
    private String businessTravel;

    @NotNull
    @Min(102)
    @Max(1499)
    @JsonProperty("DailyRate")
    private Integer dailyRate;

    @NotBlank
    @JsonProperty("Department")
    private String department;

    @NotNull
    @Min(1)
    @Max(29)
    @JsonProperty("DistanceFromHome")
    private Integer distanceFromHome;

    @NotNull
    @Min(1)
    @Max(5)
    @JsonProperty("Education")
    private Integer education;

    @NotBlank
    @JsonProperty("EducationField")
    private String educationField;

    @NotNull
    @Min(1)
    @Max(4)
    @JsonProperty("EnvironmentSatisfaction")
    private Integer environmentSatisfaction;

    @NotBlank
    @JsonProperty("Gender")
    private String gender;

    @NotNull
    @Min(30)
    @Max(100)
    @JsonProperty("HourlyRate")
    private Integer hourlyRate;

    @NotNull
    @Min(1)
    @Max(4)
    @JsonProperty("JobInvolvement")
    private Integer jobInvolvement;

    @NotNull
    @Min(1)
    @Max(5)
    @JsonProperty("JobLevel")
    private Integer jobLevel;

    @NotBlank
    @JsonProperty("JobRole")
    private String jobRole;

    @NotNull
    @Min(1)
    @Max(4)
    @JsonProperty("JobSatisfaction")
    private Integer jobSatisfaction;

    @NotBlank
    @JsonProperty("MaritalStatus")
    private String maritalStatus;

    @NotNull
    @Min(1009)
    @Max(19999)
    @JsonProperty("MonthlyIncome")
    private Integer monthlyIncome;

    @NotNull
    @Min(2094)
    @Max(26999)
    @JsonProperty("MonthlyRate")
    private Integer monthlyRate;

    @NotNull
    @Min(0)
    @Max(9)
    @JsonProperty("NumCompaniesWorked")
    private Integer numCompaniesWorked;

    @NotBlank
    @JsonProperty("OverTime")
    private String overTime;

    @NotNull
    @Min(11)
    @Max(25)
    @JsonProperty("PercentSalaryHike")
    private Integer percentSalaryHike;

    @NotNull
    @Min(3)
    @Max(4)
    @JsonProperty("PerformanceRating")
    private Integer performanceRating;

    @NotNull
    @Min(1)
    @Max(4)
    @JsonProperty("RelationshipSatisfaction")
    private Integer relationshipSatisfaction;

    @NotNull
    @Min(0)
    @Max(3)
    @JsonProperty("StockOptionLevel")
    private Integer stockOptionLevel;

    @NotNull
    @Min(0)
    @Max(40)
    @JsonProperty("TotalWorkingYears")
    private Integer totalWorkingYears;

    @NotNull
    @Min(0)
    @Max(6)
    @JsonProperty("TrainingTimesLastYear")
    private Integer trainingTimesLastYear;

    @NotNull
    @Min(1)
    @Max(4)
    @JsonProperty("WorkLifeBalance")
    private Integer workLifeBalance;

    @NotNull
    @Min(0)
    @Max(40)
    @JsonProperty("YearsAtCompany")
    private Integer yearsAtCompany;

    @NotNull
    @Min(0)
    @Max(18)
    @JsonProperty("YearsInCurrentRole")
    private Integer yearsInCurrentRole;

    @NotNull
    @Min(0)
    @Max(15)
    @JsonProperty("YearsSinceLastPromotion")
    private Integer yearsSinceLastPromotion;

    @NotNull
    @Min(0)
    @Max(17)
    @JsonProperty("YearsWithCurrManager")
    private Integer yearsWithCurrManager;


    // Getters and Setters

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBusinessTravel() {
        return businessTravel;
    }

    public void setBusinessTravel(String businessTravel) {
        this.businessTravel = businessTravel;
    }

    public Integer getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(Integer dailyRate) {
        this.dailyRate = dailyRate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getDistanceFromHome() {
        return distanceFromHome;
    }

    public void setDistanceFromHome(Integer distanceFromHome) {
        this.distanceFromHome = distanceFromHome;
    }

    public Integer getEducation() {
        return education;
    }

    public void setEducation(Integer education) {
        this.education = education;
    }

    public String getEducationField() {
        return educationField;
    }

    public void setEducationField(String educationField) {
        this.educationField = educationField;
    }

    public Integer getEnvironmentSatisfaction() {
        return environmentSatisfaction;
    }

    public void setEnvironmentSatisfaction(Integer environmentSatisfaction) {
        this.environmentSatisfaction = environmentSatisfaction;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getJobInvolvement() {
        return jobInvolvement;
    }

    public void setJobInvolvement(Integer jobInvolvement) {
        this.jobInvolvement = jobInvolvement;
    }

    public Integer getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(Integer jobLevel) {
        this.jobLevel = jobLevel;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public Integer getJobSatisfaction() {
        return jobSatisfaction;
    }

    public void setJobSatisfaction(Integer jobSatisfaction) {
        this.jobSatisfaction = jobSatisfaction;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Integer getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(Integer monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public Integer getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(Integer monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    public Integer getNumCompaniesWorked() {
        return numCompaniesWorked;
    }

    public void setNumCompaniesWorked(Integer numCompaniesWorked) {
        this.numCompaniesWorked = numCompaniesWorked;
    }

    public String getOverTime() {
        return overTime;
    }

    public void setOverTime(String overTime) {
        this.overTime = overTime;
    }

    public Integer getPercentSalaryHike() {
        return percentSalaryHike;
    }

    public void setPercentSalaryHike(Integer percentSalaryHike) {
        this.percentSalaryHike = percentSalaryHike;
    }

    public Integer getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(Integer performanceRating) {
        this.performanceRating = performanceRating;
    }

    public Integer getRelationshipSatisfaction() {
        return relationshipSatisfaction;
    }

    public void setRelationshipSatisfaction(Integer relationshipSatisfaction) {
        this.relationshipSatisfaction = relationshipSatisfaction;
    }

    public Integer getStockOptionLevel() {
        return stockOptionLevel;
    }

    public void setStockOptionLevel(Integer stockOptionLevel) {
        this.stockOptionLevel = stockOptionLevel;
    }

    public Integer getTotalWorkingYears() {
        return totalWorkingYears;
    }

    public void setTotalWorkingYears(Integer totalWorkingYears) {
        this.totalWorkingYears = totalWorkingYears;
    }

    public Integer getTrainingTimesLastYear() {
        return trainingTimesLastYear;
    }

    public void setTrainingTimesLastYear(Integer trainingTimesLastYear) {
        this.trainingTimesLastYear = trainingTimesLastYear;
    }

    public Integer getWorkLifeBalance() {
        return workLifeBalance;
    }

    public void setWorkLifeBalance(Integer workLifeBalance) {
        this.workLifeBalance = workLifeBalance;
    }

    public Integer getYearsAtCompany() {
        return yearsAtCompany;
    }

    public void setYearsAtCompany(Integer yearsAtCompany) {
        this.yearsAtCompany = yearsAtCompany;
    }

    public Integer getYearsInCurrentRole() {
        return yearsInCurrentRole;
    }

    public void setYearsInCurrentRole(Integer yearsInCurrentRole) {
        this.yearsInCurrentRole = yearsInCurrentRole;
    }

    public Integer getYearsSinceLastPromotion() {
        return yearsSinceLastPromotion;
    }

    public void setYearsSinceLastPromotion(Integer yearsSinceLastPromotion) {
        this.yearsSinceLastPromotion = yearsSinceLastPromotion;
    }

    public Integer getYearsWithCurrManager() {
        return yearsWithCurrManager;
    }

    public void setYearsWithCurrManager(Integer yearsWithCurrManager) {
        this.yearsWithCurrManager = yearsWithCurrManager;
    }
}