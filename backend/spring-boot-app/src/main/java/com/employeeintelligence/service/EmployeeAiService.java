package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import com.employeeintelligence.security.TenantContext;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import com.employeeintelligence.api.exception.MlServiceException;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.employeeintelligence.api.dto.AttritionRiskResponse;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
public class EmployeeAiService {

    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;
    private final RestClient mlRestClient;

    public EmployeeAiService(
            EmployeeRepository employeeRepository,
            RestClient.Builder restClientBuilder,
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl, TenantContext tenantContext) {

        this.employeeRepository = employeeRepository;
        this.tenantContext = tenantContext;

        this.mlRestClient = restClientBuilder
                .baseUrl(mlServiceUrl)
                .build();
    }

    public Employee getEmployee(Long employeeId) {

        return employeeRepository.findByIdAndTenantId(employeeId, tenantContext.currentTenantId())
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    public MlPredictionResponse predictEmployee(Long employeeId) {

        Employee employee = getEmployee(employeeId);

        MlPredictionRequest request = new MlPredictionRequest();

        request.setAge(employee.getAge());
        request.setBusinessTravel(employee.getBusinessTravel());
        request.setDailyRate(employee.getDailyRate());
        request.setDepartment(employee.getDepartment());
        request.setDistanceFromHome(employee.getDistanceFromHome());
        request.setEducation(employee.getEducation());
        request.setEducationField(employee.getEducationField());
        request.setEnvironmentSatisfaction(
                employee.getEnvironmentSatisfaction()
        );
        request.setGender(employee.getGender());
        request.setHourlyRate(employee.getHourlyRate());
        request.setJobInvolvement(employee.getJobInvolvement());
        request.setJobLevel(employee.getJobLevel());
        request.setJobRole(employee.getJobRole());
        request.setJobSatisfaction(employee.getJobSatisfaction());
        request.setMaritalStatus(employee.getMaritalStatus());
        request.setMonthlyIncome(employee.getMonthlyIncome());
        request.setMonthlyRate(employee.getMonthlyRate());
        request.setNumCompaniesWorked(employee.getNumCompaniesWorked());
        request.setOverTime(employee.getOverTime());
        request.setPercentSalaryHike(employee.getPercentSalaryHike());
        request.setPerformanceRating(employee.getPerformanceRating());
        request.setRelationshipSatisfaction(
                employee.getRelationshipSatisfaction()
        );
        request.setStockOptionLevel(employee.getStockOptionLevel());
        request.setTotalWorkingYears(employee.getTotalWorkingYears());
        request.setTrainingTimesLastYear(
                employee.getTrainingTimesLastYear()
        );
        request.setWorkLifeBalance(employee.getWorkLifeBalance());
        request.setYearsAtCompany(employee.getYearsAtCompany());
        request.setYearsInCurrentRole(employee.getYearsInCurrentRole());
        request.setYearsSinceLastPromotion(
                employee.getYearsSinceLastPromotion()
        );
        request.setYearsWithCurrManager(
                employee.getYearsWithCurrManager()
        );

        try {

            return mlRestClient.post()
                    .uri("/predict")
                    .body(request)
                    .retrieve()
                    .body(MlPredictionResponse.class);

        } catch (Exception exception) {

           throw new MlServiceException(
        "ML prediction service is unavailable",
        exception
);
        }
    }
    public AttritionRiskResponse assessRisk(Long employeeId) {

    MlPredictionResponse prediction =
            predictEmployee(employeeId);

    double probability =
            prediction.getAttrition_probability();

    String riskLevel;

    if (probability >= 0.70) {
        riskLevel = "High Risk";
    } else if (probability >= 0.40) {
        riskLevel = "Medium Risk";
    } else {
        riskLevel = "Low Risk";
    }

    return new AttritionRiskResponse(
            employeeId,
            prediction.getAttrition_prediction(),
            probability,
            riskLevel
    );
}

    public Map<String, Object> toPredictionMap(Employee employee) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Age", employee.getAge()); m.put("BusinessTravel", employee.getBusinessTravel()); m.put("DailyRate", employee.getDailyRate()); m.put("Department", employee.getDepartment()); m.put("DistanceFromHome", employee.getDistanceFromHome()); m.put("Education", employee.getEducation()); m.put("EducationField", employee.getEducationField()); m.put("EnvironmentSatisfaction", employee.getEnvironmentSatisfaction()); m.put("Gender", employee.getGender()); m.put("HourlyRate", employee.getHourlyRate()); m.put("JobInvolvement", employee.getJobInvolvement()); m.put("JobLevel", employee.getJobLevel()); m.put("JobRole", employee.getJobRole()); m.put("JobSatisfaction", employee.getJobSatisfaction()); m.put("MaritalStatus", employee.getMaritalStatus()); m.put("MonthlyIncome", employee.getMonthlyIncome()); m.put("MonthlyRate", employee.getMonthlyRate()); m.put("NumCompaniesWorked", employee.getNumCompaniesWorked()); m.put("OverTime", employee.getOverTime()); m.put("PercentSalaryHike", employee.getPercentSalaryHike()); m.put("PerformanceRating", employee.getPerformanceRating()); m.put("RelationshipSatisfaction", employee.getRelationshipSatisfaction()); m.put("StockOptionLevel", employee.getStockOptionLevel()); m.put("TotalWorkingYears", employee.getTotalWorkingYears()); m.put("TrainingTimesLastYear", employee.getTrainingTimesLastYear()); m.put("WorkLifeBalance", employee.getWorkLifeBalance()); m.put("YearsAtCompany", employee.getYearsAtCompany()); m.put("YearsInCurrentRole", employee.getYearsInCurrentRole()); m.put("YearsSinceLastPromotion", employee.getYearsSinceLastPromotion()); m.put("YearsWithCurrManager", employee.getYearsWithCurrManager());
        return m;
    }
}
