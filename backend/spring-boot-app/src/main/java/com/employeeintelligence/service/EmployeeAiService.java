package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.employeeintelligence.api.exception.MlServiceException;
import com.employeeintelligence.api.dto.AttritionRiskResponse;
@Service
public class EmployeeAiService {

    private final EmployeeRepository employeeRepository;
    private final RestClient mlRestClient;

    public EmployeeAiService(
            EmployeeRepository employeeRepository,
            RestClient.Builder restClientBuilder) {

        this.employeeRepository = employeeRepository;

        this.mlRestClient = restClientBuilder
                .baseUrl("http://127.0.0.1:8000")
                .build();
    }

    public Employee getEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found: " + employeeId
                        )
                );
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
}