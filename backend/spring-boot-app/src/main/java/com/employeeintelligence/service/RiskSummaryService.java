package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import com.employeeintelligence.api.dto.RiskSummaryResponse;
import com.employeeintelligence.api.exception.MlServiceException;
import com.employeeintelligence.api.model.Employee;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RiskSummaryService {
    private final EmployeeAiService employeeAiService;
    private final MlPredictionService mlPredictionService;

    public RiskSummaryService(EmployeeAiService employeeAiService, MlPredictionService mlPredictionService) {
        this.employeeAiService = employeeAiService;
        this.mlPredictionService = mlPredictionService;
    }

    public RiskSummaryResponse summarize(Long id) {
        Employee employee = employeeAiService.getEmployee(id);
        List<String> limitations = new ArrayList<>(List.of(
                "Decision-support only: never use this as a standalone employment decision.",
                "Overall risk is the deterministic equal-weight mean of attrition risk, derived performance risk (1 - class 4 score), and inverse promotion rule score; it is not calibrated."));
        var attritionResponse = employeeAiService.assessRisk(id);
        if (attritionResponse == null) throw invalidResponse("Missing attrition response");
        double attrition = bounded(attritionResponse.getProbability(), "attrition risk", limitations);
        Map<String, Object> features = employeeAiService.toPredictionMap(employee);
        Map<String, Object> performanceFeatures = new LinkedHashMap<>(features);
        performanceFeatures.remove("PerformanceRating");
        PerformancePredictionResponse performance = mlPredictionService.predictPerformance(performanceFeatures);
        PromotionPredictionResponse promotion = mlPredictionService.predictPromotion(features);
        if (performance == null || promotion == null) throw invalidResponse("Missing risk model response");
        if (performance.getClass_probabilities() == null || !performance.getClass_probabilities().containsKey("4")) {
            limitations.add("Class 4 probability is missing; performance risk uses the legacy predicted-class fallback or zero class 4 score.");
        }
        if (performance.getClass_probabilities() != null && performance.getClass_probabilities().containsKey("4")
                && performance.getClass_probabilities().get("4") == null) {
            throw invalidResponse("Missing performance class probability");
        }
        double classFour = performance.getClass_probabilities() == null
                ? (performance.getPerformance_prediction() >= 4 ? performance.getPerformance_probability() : 0)
                : performance.getClass_probabilities().getOrDefault("4", 0.0);
        double performanceRisk = bounded(1 - bounded(classFour, "performance class probability", limitations),
                "performance risk", limitations);
        double promotionReadiness = bounded(promotion.getRule_score(), "promotion rule score", limitations);
        if (performance.getLimitations() != null) limitations.addAll(performance.getLimitations());
        if (promotion.getLimitations() != null) limitations.addAll(promotion.getLimitations());

        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("attrition", versionOrUnknown(null, "attrition", limitations));
        versions.put("performance", versionOrUnknown(performance.getModel_version(), "performance", limitations));
        versions.put("promotionReadiness", versionOrUnknown(promotion.getModel_version(), "promotion readiness", limitations));
        return new RiskSummaryResponse(id, attrition, performanceRisk, promotionReadiness,
                (attrition + performanceRisk + (1 - promotionReadiness)) / 3,
                promotion.getTarget_type(), versions, limitations);
    }

    private static String versionOrUnknown(String version, String model, List<String> limitations) {
        if (version != null && !version.isBlank()) return version;
        limitations.add("Model version is not provided for " + model + "; results are not fully traceable.");
        return "unknown";
    }

    private static double bounded(double value, String field, List<String> limitations) {
        if (!Double.isFinite(value)) {
            throw invalidResponse("Non-finite " + field + " returned by the model");
        }
        if (value < 0 || value > 1) {
            limitations.add("Out-of-range " + field + " returned by the model; clamped to [0, 1].");
        }
        return Math.max(0, Math.min(1, value));
    }

    private static MlServiceException invalidResponse(String message) {
        return new MlServiceException(message, null);
    }
}
