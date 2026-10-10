package com.employeeintelligence.api.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Features accepted by the leakage-safe salary model. MonthlyIncome is intentionally absent. */
public class SalaryPredictionRequest {
    @NotNull @JsonProperty("Age") private Integer age;
    @NotBlank @JsonProperty("Department") private String department;
    @NotNull @JsonProperty("JobLevel") private Integer jobLevel;
    @NotBlank @JsonProperty("JobRole") private String jobRole;
    @NotNull @JsonProperty("TotalWorkingYears") private Integer totalWorkingYears;
    @NotNull @JsonProperty("YearsAtCompany") private Integer yearsAtCompany;
    @NotNull @JsonProperty("Education") private Integer education;
    @NotNull @JsonProperty("PercentSalaryHike") private Integer percentSalaryHike;
    @NotNull @JsonProperty("PerformanceRating") private Integer performanceRating;
    @NotBlank @JsonProperty("Gender") private String gender;
    @NotBlank @JsonProperty("MaritalStatus") private String maritalStatus;
    @NotBlank @JsonProperty("OverTime") private String overTime;
    @NotNull @JsonProperty("StockOptionLevel") private Integer stockOptionLevel;
    public Integer getAge(){return age;} public void setAge(Integer v){age=v;} public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
    public Integer getJobLevel(){return jobLevel;} public void setJobLevel(Integer v){jobLevel=v;} public String getJobRole(){return jobRole;} public void setJobRole(String v){jobRole=v;}
    public Integer getTotalWorkingYears(){return totalWorkingYears;} public void setTotalWorkingYears(Integer v){totalWorkingYears=v;} public Integer getYearsAtCompany(){return yearsAtCompany;} public void setYearsAtCompany(Integer v){yearsAtCompany=v;}
    public Integer getEducation(){return education;} public void setEducation(Integer v){education=v;} public Integer getPercentSalaryHike(){return percentSalaryHike;} public void setPercentSalaryHike(Integer v){percentSalaryHike=v;}
    public Integer getPerformanceRating(){return performanceRating;} public void setPerformanceRating(Integer v){performanceRating=v;} public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public String getMaritalStatus(){return maritalStatus;} public void setMaritalStatus(String v){maritalStatus=v;} public String getOverTime(){return overTime;} public void setOverTime(String v){overTime=v;}
    public Integer getStockOptionLevel(){return stockOptionLevel;} public void setStockOptionLevel(Integer v){stockOptionLevel=v;}
}
