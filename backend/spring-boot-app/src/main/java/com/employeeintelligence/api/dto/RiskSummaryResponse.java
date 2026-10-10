package com.employeeintelligence.api.dto;
import java.util.List; import java.util.Map;
public record RiskSummaryResponse(Long employeeId, double attritionRisk, double performanceRisk, double promotionReadiness, double overallRisk, String promotionTargetType, Map<String,String> modelVersions, List<String> limitations) {}
