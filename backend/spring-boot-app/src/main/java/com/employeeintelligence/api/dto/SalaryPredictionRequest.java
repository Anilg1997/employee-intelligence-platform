package com.employeeintelligence.api.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/** Features accepted by the leakage-safe salary model. MonthlyIncome is intentionally absent. */
public class SalaryPredictionRequest {
    @NotNull @Min(18) @Max(60) @JsonProperty("Age") private Integer age;
    @NotBlank @Pattern(regexp="Sales|Research & Development|Human Resources") @JsonProperty("Department") private String department;
    @NotNull @Min(1) @Max(5) @JsonProperty("JobLevel") private Integer jobLevel;
    @NotBlank @Pattern(regexp="Sales Executive|Research Scientist|Laboratory Technician|Manufacturing Director|Healthcare Representative|Manager|Sales Representative|Research Director|Human Resources") @JsonProperty("JobRole") private String jobRole;
    @NotNull @Min(0) @Max(40) @JsonProperty("TotalWorkingYears") private Integer totalWorkingYears;
    @NotNull @Min(0) @Max(40) @JsonProperty("YearsAtCompany") private Integer yearsAtCompany;
    @NotNull @Min(1) @Max(5) @JsonProperty("Education") private Integer education;
    @NotNull @Min(11) @Max(25) @JsonProperty("PercentSalaryHike") private Integer percentSalaryHike;
    @NotNull @Min(1) @Max(4) @JsonProperty("PerformanceRating") private Integer performanceRating;
    @NotBlank @Pattern(regexp="Male|Female") @JsonProperty("Gender") private String gender;
    @NotBlank @Pattern(regexp="Single|Married|Divorced") @JsonProperty("MaritalStatus") private String maritalStatus;
    @NotBlank @Pattern(regexp="Yes|No") @JsonProperty("OverTime") private String overTime;
    @NotNull @Min(0) @Max(3) @JsonProperty("StockOptionLevel") private Integer stockOptionLevel;
    public Integer getAge(){return age;} public void setAge(Integer v){age=v;} public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
    public Integer getJobLevel(){return jobLevel;} public void setJobLevel(Integer v){jobLevel=v;} public String getJobRole(){return jobRole;} public void setJobRole(String v){jobRole=v;}
    public Integer getTotalWorkingYears(){return totalWorkingYears;} public void setTotalWorkingYears(Integer v){totalWorkingYears=v;} public Integer getYearsAtCompany(){return yearsAtCompany;} public void setYearsAtCompany(Integer v){yearsAtCompany=v;}
    public Integer getEducation(){return education;} public void setEducation(Integer v){education=v;} public Integer getPercentSalaryHike(){return percentSalaryHike;} public void setPercentSalaryHike(Integer v){percentSalaryHike=v;}
    public Integer getPerformanceRating(){return performanceRating;} public void setPerformanceRating(Integer v){performanceRating=v;} public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public String getMaritalStatus(){return maritalStatus;} public void setMaritalStatus(String v){maritalStatus=v;} public String getOverTime(){return overTime;} public void setOverTime(String v){overTime=v;}
    public Integer getStockOptionLevel(){return stockOptionLevel;} public void setStockOptionLevel(Integer v){stockOptionLevel=v;}
}
