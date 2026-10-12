package com.employeeintelligence.api.dto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RiskSummaryResponse(Long employeeId, double attritionRisk, double performanceRisk,
                                  double promotionReadiness, double overallRisk,
                                  String promotionTargetType, Map<String, String> modelVersions,
                                  List<String> limitations) {
    public RiskSummaryResponse {
        modelVersions = modelVersions == null ? Map.of()
                : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(modelVersions));
        limitations = limitations == null ? List.of() : List.copyOf(limitations);
    }
}
