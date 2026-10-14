package com.employeeintelligence.api.dto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RiskSummaryResponse(Long employeeId, double attritionRisk, double performanceRisk,
                                  double promotionReadiness, double overallRisk,
                                  String riskBand, String interpretation,
                                  String formula, String formulaVersion,
                                  String promotionTargetType, Map<String, String> modelVersions,
                                  List<String> limitations) {
    public RiskSummaryResponse {
        if (employeeId == null || !Double.isFinite(attritionRisk) || !Double.isFinite(performanceRisk)
                || !Double.isFinite(promotionReadiness) || !Double.isFinite(overallRisk)
                || attritionRisk < 0 || attritionRisk > 1 || performanceRisk < 0 || performanceRisk > 1
                || promotionReadiness < 0 || promotionReadiness > 1 || overallRisk < 0 || overallRisk > 1) {
            throw new IllegalArgumentException("Risk summary contains invalid values");
        }
        if (riskBand == null || interpretation == null || formula == null || formulaVersion == null
                || riskBand.isBlank() || interpretation.isBlank() || formula.isBlank() || formulaVersion.isBlank()) {
            throw new IllegalArgumentException("Risk summary metadata is required");
        }
        double expectedOverall = (attritionRisk + performanceRisk + (1 - promotionReadiness)) / 3;
        if (Math.abs(expectedOverall - overallRisk) > 1e-9) {
            throw new IllegalArgumentException("Risk summary overall risk is inconsistent with its components");
        }
        modelVersions = modelVersions == null ? Map.of()
                : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(modelVersions));
        limitations = limitations == null ? List.of() : List.copyOf(new java.util.LinkedHashSet<>(limitations));
    }
}
