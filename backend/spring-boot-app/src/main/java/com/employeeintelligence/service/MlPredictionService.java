package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.SalaryPredictionRequest;
import com.employeeintelligence.api.dto.SalaryPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionRequest;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import com.employeeintelligence.api.exception.MlServiceException;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

@Service
public class MlPredictionService {

    private final RestClient restClient;

    @Autowired
    public MlPredictionService(
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl,
            @Value("${ml.service.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${ml.service.read-timeout-ms:5000}") int readTimeoutMs) {
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("ML service timeouts must be positive");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // Constructor used for unit testing
    MlPredictionService(RestClient restClient) {
        this.restClient = restClient;
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {
        try {
            MlPredictionResponse response = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlPredictionResponse.class);
            validate(response);
            return response;
        } catch (RestClientException exception) {
            throw new MlServiceException("Attrition prediction service unavailable", exception);
        }
    }

    private static void validate(MlPredictionResponse response) {
        if (response == null || !("Yes".equals(response.getAttrition_prediction())
                || "No".equals(response.getAttrition_prediction()))) {
            throw new MlServiceException("Invalid attrition prediction response", null);
        }
        Double probability = response.getAttrition_probability();
        Double threshold = response.getDecision_threshold();
        if (probability == null || !Double.isFinite(probability) || probability < 0 || probability > 1
                || threshold == null || !Double.isFinite(threshold) || threshold < 0 || threshold > 1
                || isBlank(response.getModel_version()) || isBlank(response.getDataset())
                || isBlank(response.getTraining_provenance()) || response.getLimitations() == null
                || response.getLimitations().stream().anyMatch(item -> item == null || item.isBlank())) {
            throw new MlServiceException("Invalid attrition prediction response metadata", null);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public SalaryPredictionResponse predictSalary(SalaryPredictionRequest request) {
        try {
            SalaryPredictionResponse response = restClient.post().uri("/predict/salary").contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve().body(SalaryPredictionResponse.class);
            validateSalary(response);
            return response;
        } catch (RestClientException exception) {
            throw new MlServiceException("Salary prediction service unavailable", exception);
        }
    }

    private static void validateSalary(SalaryPredictionResponse response) {
        if (response == null || response.getEstimated_monthly_income() == null
                || !Double.isFinite(response.getEstimated_monthly_income())
                || response.getEstimated_monthly_income() < 0 || isBlank(response.getModel_version())
                || isBlank(response.getAlgorithm()) || isBlank(response.getDataset())
                || isBlank(response.getTraining_provenance()) || response.getMetrics() == null
                || !response.getMetrics().keySet().equals(Set.of("validation", "test")) || response.getLimitations() == null
                || response.getLimitations().isEmpty()
                || response.getLimitations().stream().anyMatch(item -> item == null || item.isBlank())) {
            throw new MlServiceException("Invalid salary prediction response", null);
        }
        response.getMetrics().forEach((partition, values) -> {
            if (isBlank(partition) || values == null || !values.keySet().equals(Set.of("mae", "rmse", "r2"))
                    || values.values().stream().anyMatch(value -> value == null || !Double.isFinite(value))
                    || values.get("mae") < 0 || values.get("rmse") < 0 || values.get("r2") > 1) {
                throw new MlServiceException("Invalid salary prediction response metrics", null);
            }
        });
    }

    public PerformancePredictionResponse predictPerformance(PerformancePredictionRequest request) {
        Set<String> expected = Set.of("Age", "BusinessTravel", "DailyRate", "Department", "DistanceFromHome", "Education", "EducationField", "EnvironmentSatisfaction", "Gender", "HourlyRate", "JobInvolvement", "JobLevel", "JobRole", "JobSatisfaction", "MaritalStatus", "MonthlyIncome", "MonthlyRate", "NumCompaniesWorked", "OverTime", "PercentSalaryHike", "RelationshipSatisfaction", "StockOptionLevel", "TotalWorkingYears", "TrainingTimesLastYear", "WorkLifeBalance", "YearsAtCompany", "YearsInCurrentRole", "YearsSinceLastPromotion", "YearsWithCurrManager");
        if (request == null || !request.features().keySet().equals(expected)) throw new MlServiceException("Invalid performance prediction request", null);
        try {
            PerformancePredictionResponse response = restClient.post().uri("/predict/performance").contentType(MediaType.APPLICATION_JSON)
                    .body(request.features()).retrieve().body(PerformancePredictionResponse.class);
            validatePerformance(response);
            return response;
        } catch (RestClientException exception) {
            throw new MlServiceException("Performance prediction service unavailable", exception);
        }
    }

    /** Compatibility adapter for callers that already hold a decoded feature map. */
    public PerformancePredictionResponse predictPerformance(Map<String, Object> request) {
        try {
            PerformancePredictionResponse response = restClient.post().uri("/predict/performance").contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve().body(PerformancePredictionResponse.class);
            validatePerformance(response);
            return response;
        } catch (RestClientException exception) {
            throw new MlServiceException("Performance prediction service unavailable", exception);
        }
    }

    private static void validatePerformance(PerformancePredictionResponse response) {
        if (response == null || response.getPerformance_prediction() < 1 || response.getPerformance_prediction() > 4
                || !Double.isFinite(response.getPerformance_probability()) || response.getPerformance_probability() < 0 || response.getPerformance_probability() > 1
                || response.getClass_probabilities() == null || response.getClass_probabilities().isEmpty()
                || response.getClass_probabilities().keySet().stream().anyMatch(k -> !Set.of("1", "2", "3", "4").contains(k))
                || response.getClass_probabilities().values().stream().anyMatch(v -> v == null || !Double.isFinite(v) || v < 0 || v > 1)
                || Math.abs(response.getClass_probabilities().values().stream().mapToDouble(Double::doubleValue).sum() - 1) > 1e-6
                || Math.abs(response.getPerformance_probability() - response.getClass_probabilities().getOrDefault(String.valueOf(response.getPerformance_prediction()), -1d)) > 1e-6
                || isBlank(response.getModel_version()) || isBlank(response.getAlgorithm()) || isBlank(response.getDataset()) || isBlank(response.getTraining_provenance())
                || response.getMetrics() == null || !response.getMetrics().keySet().equals(Set.of("validation", "test"))
                || response.getMetrics().values().stream().anyMatch(v -> v == null || !v.keySet().equals(Set.of("accuracy", "balanced_accuracy", "f1_macro")) || v.values().stream().anyMatch(x -> x == null || !Double.isFinite(x) || x < 0 || x > 1))
                || response.getLimitations() == null || response.getLimitations().isEmpty() || response.getLimitations().stream().anyMatch(x -> x == null || x.isBlank()))
            throw new MlServiceException("Invalid performance prediction response", null);
    }

    public PromotionPredictionResponse predictPromotion(Map<String, Object> request) {
        return restClient.post().uri("/predict/promotion").contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve().body(PromotionPredictionResponse.class);
    }
}
