package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.SalaryPredictionRequest;
import com.employeeintelligence.api.dto.SalaryPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import java.util.Map;
import com.employeeintelligence.api.service.MlPredictionService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ml")
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;

    public MlPredictionController(MlPredictionService mlPredictionService) {
        this.mlPredictionService = mlPredictionService;
    }

    @PostMapping("/predict")
    public MlPredictionResponse predict(
            @RequestBody @Valid MlPredictionRequest request) {

        return mlPredictionService.predict(request);
    }

    @PostMapping("/predict/salary")
    public SalaryPredictionResponse predictSalary(@RequestBody @Valid SalaryPredictionRequest request) {
        return mlPredictionService.predictSalary(request);
    }

    @PostMapping("/predict/performance")
    public PerformancePredictionResponse predictPerformance(@RequestBody Map<String, Object> request) {
        return mlPredictionService.predictPerformance(request);
    }

    @PostMapping("/predict/promotion")
    public PromotionPredictionResponse predictPromotion(@RequestBody Map<String, Object> request) {
        return mlPredictionService.predictPromotion(request);
    }
}
