package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.SalaryPredictionRequest;
import com.employeeintelligence.api.dto.SalaryPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import java.util.Map;
import com.employeeintelligence.api.service.MlPredictionService;
import com.employeeintelligence.api.service.ModelRegistryService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.employeeintelligence.api.dto.ModelMetadata;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.employeeintelligence.api.service.AuditService;

@RestController
@RequestMapping("/api/ml")
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;
    private final AuditService auditService;

    public MlPredictionController(MlPredictionService mlPredictionService, AuditService auditService) {
        this.mlPredictionService = mlPredictionService;
        this.auditService = auditService;
    }

    @PostMapping("/predict")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public MlPredictionResponse predict(
            @RequestBody @Valid MlPredictionRequest request) {

        var response = mlPredictionService.predict(request);
        auditService.record("ML_PREDICTION", "model", "predict", "SUCCESS", "prediction completed"); return response;
    }

    @GetMapping("/models")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_ANALYST')")
    public List<ModelMetadata> models(ModelRegistryService registry) { return registry.list(); }

    @PostMapping("/predict/salary")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public SalaryPredictionResponse predictSalary(@RequestBody @Valid SalaryPredictionRequest request) {
        var response = mlPredictionService.predictSalary(request); auditService.record("ML_PREDICTION", "model", "salary", "SUCCESS", "prediction completed"); return response;
    }

    @PostMapping("/predict/performance")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public PerformancePredictionResponse predictPerformance(@RequestBody Map<String, Object> request) {
        var response = mlPredictionService.predictPerformance(request); auditService.record("ML_PREDICTION", "model", "performance", "SUCCESS", "prediction completed"); return response;
    }

    @PostMapping("/predict/promotion")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public PromotionPredictionResponse predictPromotion(@RequestBody Map<String, Object> request) {
        var response = mlPredictionService.predictPromotion(request); auditService.record("ML_PREDICTION", "model", "promotion", "SUCCESS", "prediction completed"); return response;
    }
}
