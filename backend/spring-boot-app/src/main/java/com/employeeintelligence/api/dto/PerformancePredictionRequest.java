package com.employeeintelligence.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict leakage-safe performance features; PerformanceRating is deliberately absent. */
@JsonIgnoreProperties(ignoreUnknown = false)
public class PerformancePredictionRequest {
    @NotNull @Min(18) @Max(60) @JsonProperty("Age") private Integer age;
    @NotBlank @Pattern(regexp="Travel_Rarely|Travel_Frequently|Non-Travel") @JsonProperty("BusinessTravel") private String businessTravel;
    @NotNull @Min(102) @Max(1499) @JsonProperty("DailyRate") private Integer dailyRate;
    @NotBlank @Pattern(regexp="Sales|Research & Development|Human Resources") @JsonProperty("Department") private String department;
    @NotNull @Min(1) @Max(29) @JsonProperty("DistanceFromHome") private Integer distanceFromHome;
    @NotNull @Min(1) @Max(5) @JsonProperty("Education") private Integer education;
    @NotBlank @Pattern(regexp="Life Sciences|Medical|Marketing|Technical Degree|Human Resources|Other") @JsonProperty("EducationField") private String educationField;
    @NotNull @Min(1) @Max(4) @JsonProperty("EnvironmentSatisfaction") private Integer environmentSatisfaction;
    @NotBlank @Pattern(regexp="Male|Female") @JsonProperty("Gender") private String gender;
    @NotNull @Min(30) @Max(100) @JsonProperty("HourlyRate") private Integer hourlyRate;
    @NotNull @Min(1) @Max(4) @JsonProperty("JobInvolvement") private Integer jobInvolvement;
    @NotNull @Min(1) @Max(5) @JsonProperty("JobLevel") private Integer jobLevel;
    @NotBlank @Pattern(regexp="Sales Executive|Research Scientist|Laboratory Technician|Manufacturing Director|Healthcare Representative|Manager|Sales Representative|Research Director|Human Resources") @JsonProperty("JobRole") private String jobRole;
    @NotNull @Min(1) @Max(4) @JsonProperty("JobSatisfaction") private Integer jobSatisfaction;
    @NotBlank @Pattern(regexp="Single|Married|Divorced") @JsonProperty("MaritalStatus") private String maritalStatus;
    @NotNull @Min(1009) @Max(19999) @JsonProperty("MonthlyIncome") private Integer monthlyIncome;
    @NotNull @Min(2094) @Max(26999) @JsonProperty("MonthlyRate") private Integer monthlyRate;
    @NotNull @Min(0) @Max(9) @JsonProperty("NumCompaniesWorked") private Integer numCompaniesWorked;
    @NotBlank @Pattern(regexp="Yes|No") @JsonProperty("OverTime") private String overTime;
    @NotNull @Min(11) @Max(25) @JsonProperty("PercentSalaryHike") private Integer percentSalaryHike;
    @NotNull @Min(1) @Max(4) @JsonProperty("RelationshipSatisfaction") private Integer relationshipSatisfaction;
    @NotNull @Min(0) @Max(3) @JsonProperty("StockOptionLevel") private Integer stockOptionLevel;
    @NotNull @Min(0) @Max(40) @JsonProperty("TotalWorkingYears") private Integer totalWorkingYears;
    @NotNull @Min(0) @Max(6) @JsonProperty("TrainingTimesLastYear") private Integer trainingTimesLastYear;
    @NotNull @Min(1) @Max(4) @JsonProperty("WorkLifeBalance") private Integer workLifeBalance;
    @NotNull @Min(0) @Max(40) @JsonProperty("YearsAtCompany") private Integer yearsAtCompany;
    @NotNull @Min(0) @Max(18) @JsonProperty("YearsInCurrentRole") private Integer yearsInCurrentRole;
    @NotNull @Min(0) @Max(15) @JsonProperty("YearsSinceLastPromotion") private Integer yearsSinceLastPromotion;
    @NotNull @Min(0) @Max(17) @JsonProperty("YearsWithCurrManager") private Integer yearsWithCurrManager;

    public Map<String, Object> features() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("Age", age); values.put("BusinessTravel", businessTravel); values.put("DailyRate", dailyRate);
        values.put("Department", department); values.put("DistanceFromHome", distanceFromHome); values.put("Education", education);
        values.put("EducationField", educationField); values.put("EnvironmentSatisfaction", environmentSatisfaction); values.put("Gender", gender);
        values.put("HourlyRate", hourlyRate); values.put("JobInvolvement", jobInvolvement); values.put("JobLevel", jobLevel);
        values.put("JobRole", jobRole); values.put("JobSatisfaction", jobSatisfaction); values.put("MaritalStatus", maritalStatus);
        values.put("MonthlyIncome", monthlyIncome); values.put("MonthlyRate", monthlyRate); values.put("NumCompaniesWorked", numCompaniesWorked);
        values.put("OverTime", overTime); values.put("PercentSalaryHike", percentSalaryHike); values.put("RelationshipSatisfaction", relationshipSatisfaction);
        values.put("StockOptionLevel", stockOptionLevel); values.put("TotalWorkingYears", totalWorkingYears); values.put("TrainingTimesLastYear", trainingTimesLastYear);
        values.put("WorkLifeBalance", workLifeBalance); values.put("YearsAtCompany", yearsAtCompany); values.put("YearsInCurrentRole", yearsInCurrentRole);
        values.put("YearsSinceLastPromotion", yearsSinceLastPromotion); values.put("YearsWithCurrManager", yearsWithCurrManager);
        return values;
    }
}
