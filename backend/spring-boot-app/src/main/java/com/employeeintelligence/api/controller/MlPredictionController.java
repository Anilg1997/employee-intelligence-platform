package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.SalaryPredictionRequest;
import com.employeeintelligence.api.dto.SalaryPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionRequest;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionRequest;
import java.util.Map;
import com.employeeintelligence.api.service.MlPredictionService;
import com.employeeintelligence.api.service.ModelRegistryService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.employeeintelligence.api.dto.ModelMetadata;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.employeeintelligence.api.service.AuditService;
import com.employeeintelligence.api.service.OutboxService;
import com.employeeintelligence.api.notification.NotificationEvent;
import com.employeeintelligence.api.notification.NotificationEventType;
import com.employeeintelligence.security.AuthenticatedUser;
import com.employeeintelligence.security.TenantContext;
import java.util.UUID;

@RestController
@RequestMapping("/api/ml")
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;
    private final AuditService auditService;
    private final ModelRegistryService registry;
    private final OutboxService outbox;
    private final TenantContext tenantContext;

    public MlPredictionController(MlPredictionService mlPredictionService, AuditService auditService,
                                  ModelRegistryService registry, OutboxService outbox, TenantContext tenantContext) {
        this.mlPredictionService = mlPredictionService;
        this.auditService = auditService;
        this.registry = registry;
        this.outbox = outbox;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/predict")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public MlPredictionResponse predict(
            @RequestBody @Valid MlPredictionRequest request) {

        var response = mlPredictionService.predict(request);
        auditService.record("ML_PREDICTION", "model", "predict", "SUCCESS",
                String.format(Locale.ROOT, "model_version=%s;threshold=%.4f;probability=%.4f;prediction=%s",
                        response.getModel_version(), response.getDecision_threshold(),
                        response.getAttrition_probability(), response.getAttrition_prediction()));
        notifyPrediction();
        return response;
    }

    @GetMapping("/models")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public List<ModelMetadata> models() { return registry.list(); }

    @PostMapping("/predict/salary")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public SalaryPredictionResponse predictSalary(@RequestBody @Valid SalaryPredictionRequest request) {
        var response = mlPredictionService.predictSalary(request);
        auditService.record("ML_PREDICTION", "model", "salary", "SUCCESS",
                String.format(Locale.ROOT, "model_version=%s;algorithm=%s;dataset=%s;estimated_monthly_income=%.2f",
                        response.getModel_version(), response.getAlgorithm(), response.getDataset(),
                        response.getEstimated_monthly_income()));
        notifyPrediction();
        return response;
    }

    @PostMapping("/predict/performance")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public PerformancePredictionResponse predictPerformance(@RequestBody @Valid PerformancePredictionRequest request) {
        var response = mlPredictionService.predictPerformance(request);
        auditService.record("ML_PREDICTION", "model", "performance", "SUCCESS",
                String.format(Locale.ROOT, "model_version=%s;algorithm=%s;dataset=%s;prediction=%d;probability=%.4f",
                        response.getModel_version(), response.getAlgorithm(), response.getDataset(),
                        response.getPerformance_prediction(), response.getPerformance_probability()));
        notifyPrediction();
        return response;
    }

    @PostMapping("/predict/promotion")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public PromotionPredictionResponse predictPromotion(@RequestBody @Valid PromotionPredictionRequest request) {
        var response = mlPredictionService.predictPromotion(request);
        auditService.record("ML_PREDICTION", "model", "promotion", "SUCCESS",
                String.format(Locale.ROOT, "model_version=%s;target_type=%s;threshold=%.4f;rule_score=%.4f;prediction=%s;rules=%s",
                        response.getModel_version(), response.getTarget_type(), response.getDecision_threshold(),
                        response.getRule_score(), response.getPromotion_prediction(), response.getRule_checks()));
        notifyPrediction();
        return response;
    }

    private void notifyPrediction() {
        var user = AuthenticatedUser.current();
        outbox.enqueueNotification(new NotificationEvent(1, NotificationEventType.PREDICTION_COMPLETED,
                "prediction-" + UUID.randomUUID(), tenantContext.currentTenantId(), user.subject()));
    }
}
