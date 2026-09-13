package com.employeeintelligence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MlPredictionRequest {

    @JsonProperty("Age")
    private int age;

    @JsonProperty("BusinessTravel")
    private String businessTravel;

    @JsonProperty("DailyRate")
    private int dailyRate;

    @JsonProperty("Department")
    private String department;

    @JsonProperty("DistanceFromHome")
    private int distanceFromHome;

    @JsonProperty("Education")
    private int education;

    @JsonProperty("EducationField")
    private String educationField;

    @JsonProperty("EnvironmentSatisfaction")
    private int environmentSatisfaction;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("HourlyRate")
    private int hourlyRate;

    @JsonProperty("JobInvolvement")
    private int jobInvolvement;

    @JsonProperty("JobLevel")
    private int jobLevel;

    @JsonProperty("JobRole")
    private String jobRole;

    @JsonProperty("JobSatisfaction")
    private int jobSatisfaction;

    @JsonProperty("MaritalStatus")
    private String maritalStatus;

    @JsonProperty("MonthlyIncome")
    private int monthlyIncome;

    @JsonProperty("MonthlyRate")
    private int monthlyRate;

    @JsonProperty("NumCompaniesWorked")
    private int numCompaniesWorked;

    @JsonProperty("OverTime")
    private String overTime;

    @JsonProperty("PercentSalaryHike")
    private int percentSalaryHike;

    @JsonProperty("PerformanceRating")
    private int performanceRating;

    @JsonProperty("RelationshipSatisfaction")
    private int relationshipSatisfaction;

    @JsonProperty("StockOptionLevel")
    private int stockOptionLevel;

    @JsonProperty("TotalWorkingYears")
    private int totalWorkingYears;

    @JsonProperty("TrainingTimesLastYear")
    private int trainingTimesLastYear;

    @JsonProperty("WorkLifeBalance")
    private int workLifeBalance;

    @JsonProperty("YearsAtCompany")
    private int yearsAtCompany;

    @JsonProperty("YearsInCurrentRole")
    private int yearsInCurrentRole;

    @JsonProperty("YearsSinceLastPromotion")
    private int yearsSinceLastPromotion;

    @JsonProperty("YearsWithCurrManager")
    private int yearsWithCurrManager;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBusinessTravel() {
        return businessTravel;
    }

    public void setBusinessTravel(String businessTravel) {
        this.businessTravel = businessTravel;
    }

    public int getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(int dailyRate) {
        this.dailyRate = dailyRate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getDistanceFromHome() {
        return distanceFromHome;
    }

    public void setDistanceFromHome(int distanceFromHome) {
        this.distanceFromHome = distanceFromHome;
    }

    public int getEducation() {
        return education;
    }

    public void setEducation(int education) {
        this.education = education;
    }

    public String getEducationField() {
        return educationField;
    }

    public void setEducationField(String educationField) {
        this.educationField = educationField;
    }

    public int getEnvironmentSatisfaction() {
        return environmentSatisfaction;
    }

    public void setEnvironmentSatisfaction(int environmentSatisfaction) {
        this.environmentSatisfaction = environmentSatisfaction;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(int hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getJobInvolvement() {
        return jobInvolvement;
    }

    public void setJobInvolvement(int jobInvolvement) {
        this.jobInvolvement = jobInvolvement;
    }

    public int getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(int jobLevel) {
        this.jobLevel = jobLevel;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public int getJobSatisfaction() {
        return jobSatisfaction;
    }

    public void setJobSatisfaction(int jobSatisfaction) {
        this.jobSatisfaction = jobSatisfaction;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public int getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(int monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public int getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(int monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    public int getNumCompaniesWorked() {
        return numCompaniesWorked;
    }

    public void setNumCompaniesWorked(int numCompaniesWorked) {
        this.numCompaniesWorked = numCompaniesWorked;
    }

    public String getOverTime() {
        return overTime;
    }

    public void setOverTime(String overTime) {
        this.overTime = overTime;
    }

    public int getPercentSalaryHike() {
        return percentSalaryHike;
    }

    public void setPercentSalaryHike(int percentSalaryHike) {
        this.percentSalaryHike = percentSalaryHike;
    }

    public int getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(int performanceRating) {
        this.performanceRating = performanceRating;
    }

    public int getRelationshipSatisfaction() {
        return relationshipSatisfaction;
    }

    public void setRelationshipSatisfaction(int relationshipSatisfaction) {
        this.relationshipSatisfaction = relationshipSatisfaction;
    }

    public int getStockOptionLevel() {
        return stockOptionLevel;
    }

    public void setStockOptionLevel(int stockOptionLevel) {
        this.stockOptionLevel = stockOptionLevel;
    }

    public int getTotalWorkingYears() {
        return totalWorkingYears;
    }

    public void setTotalWorkingYears(int totalWorkingYears) {
        this.totalWorkingYears = totalWorkingYears;
    }

    public int getTrainingTimesLastYear() {
        return trainingTimesLastYear;
    }

    public void setTrainingTimesLastYear(int trainingTimesLastYear) {
        this.trainingTimesLastYear = trainingTimesLastYear;
    }

    public int getWorkLifeBalance() {
        return workLifeBalance;
    }

    public void setWorkLifeBalance(int workLifeBalance) {
        this.workLifeBalance = workLifeBalance;
    }

    public int getYearsAtCompany() {
        return yearsAtCompany;
    }

    public void setYearsAtCompany(int yearsAtCompany) {
        this.yearsAtCompany = yearsAtCompany;
    }

    public int getYearsInCurrentRole() {
        return yearsInCurrentRole;
    }

    public void setYearsInCurrentRole(int yearsInCurrentRole) {
        this.yearsInCurrentRole = yearsInCurrentRole;
    }

    public int getYearsSinceLastPromotion() {
        return yearsSinceLastPromotion;
    }

    public void setYearsSinceLastPromotion(int yearsSinceLastPromotion) {
        this.yearsSinceLastPromotion = yearsSinceLastPromotion;
    }

    public int getYearsWithCurrManager() {
        return yearsWithCurrManager;
    }

    public void setYearsWithCurrManager(int yearsWithCurrManager) {
        this.yearsWithCurrManager = yearsWithCurrManager;
    }
}