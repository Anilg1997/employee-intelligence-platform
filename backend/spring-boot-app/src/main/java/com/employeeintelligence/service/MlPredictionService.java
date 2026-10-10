package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.SalaryPredictionRequest;
import com.employeeintelligence.api.dto.SalaryPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MlPredictionService {

    private final RestClient restClient;

    @Autowired
    public MlPredictionService(
            @Value("${ml.service.url}") String mlServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    // Constructor used for unit testing
    MlPredictionService(RestClient restClient) {
        this.restClient = restClient;
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {

        return restClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MlPredictionResponse.class);
    }

    public SalaryPredictionResponse predictSalary(SalaryPredictionRequest request) {
        return restClient.post().uri("/predict/salary").contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve().body(SalaryPredictionResponse.class);
    }

    public PerformancePredictionResponse predictPerformance(Map<String, Object> request) {
        return restClient.post().uri("/predict/performance").contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve().body(PerformancePredictionResponse.class);
    }
}
