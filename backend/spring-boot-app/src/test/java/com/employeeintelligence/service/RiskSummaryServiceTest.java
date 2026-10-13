package com.employeeintelligence.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyMap;

import com.employeeintelligence.api.dto.AttritionRiskResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import com.employeeintelligence.api.dto.RiskSummaryResponse;
import com.employeeintelligence.api.exception.MlServiceException;
import com.employeeintelligence.api.model.Employee;
import java.util.List;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

class RiskSummaryServiceTest {
    @Test
    void boundsRiskValuesAndMarksMissingVersions() {
        EmployeeAiService employees = mock(EmployeeAiService.class);
        MlPredictionService predictions = mock(MlPredictionService.class);
        Employee employee = mock(Employee.class);
        when(employees.getEmployee(7L)).thenReturn(employee);
        when(employees.assessRisk(7L)).thenReturn(new AttritionRiskResponse(7L, "yes", 1.5, "High Risk"));
        when(employees.toPredictionMap(employee)).thenReturn(Map.of());
        PerformancePredictionResponse performance = new PerformancePredictionResponse();
        performance.setPerformance_prediction(4);
        performance.setPerformance_probability(-0.2);
        when(predictions.predictPerformance(Map.of())).thenReturn(performance);
        PromotionPredictionResponse promotion = new PromotionPredictionResponse();
        promotion.setRule_score(1.5);
        when(predictions.predictPromotion(Map.of())).thenReturn(promotion);

        RiskSummaryResponse response = new RiskSummaryService(employees, predictions).summarize(7L);

        assertEquals(1.0, response.attritionRisk());
        assertEquals(1.0, response.performanceRisk());
        assertEquals(1.0, response.promotionReadiness());
        assertEquals("unknown", response.modelVersions().get("performance"));
        assertEquals("unknown", response.modelVersions().get("promotionReadiness"));
        assertEquals("unknown", response.modelVersions().get("attrition"));
        assertEquals(true, response.limitations().stream().anyMatch(value -> value.contains("not fully traceable")));
    }

    @Test
    void rejectsNonFinitePerformanceProbability() {
        EmployeeAiService employees = mock(EmployeeAiService.class);
        MlPredictionService predictions = mock(MlPredictionService.class);
        Employee employee = mock(Employee.class);
        when(employees.getEmployee(7L)).thenReturn(employee);
        when(employees.assessRisk(7L)).thenReturn(new AttritionRiskResponse(7L, "yes", 0.2, "Low Risk"));
        when(employees.toPredictionMap(employee)).thenReturn(Map.of());
        PerformancePredictionResponse performance = new PerformancePredictionResponse();
        performance.setPerformance_prediction(4);
        performance.setPerformance_probability(Double.NaN);
        when(predictions.predictPerformance(Map.of())).thenReturn(performance);
        PromotionPredictionResponse promotion = new PromotionPredictionResponse();
        when(predictions.predictPromotion(Map.of())).thenReturn(promotion);

        assertThrows(MlServiceException.class, () -> new RiskSummaryService(employees, predictions).summarize(7L));
    }

    @Test
    void removesPerformanceTargetBeforePerformanceInference() {
        EmployeeAiService employees = mock(EmployeeAiService.class);
        MlPredictionService predictions = mock(MlPredictionService.class);
        Employee employee = mock(Employee.class);
        Map<String, Object> features = Map.of("Age", 35, "PerformanceRating", 4);
        when(employees.getEmployee(7L)).thenReturn(employee);
        when(employees.assessRisk(7L)).thenReturn(new AttritionRiskResponse(7L, "no", 0.2, "Low Risk"));
        when(employees.toPredictionMap(employee)).thenReturn(features);
        PerformancePredictionResponse performance = new PerformancePredictionResponse();
        performance.setPerformance_prediction(3);
        performance.setPerformance_probability(0.8);
        performance.setClass_probabilities(Map.of("3", 0.8, "4", 0.2));
        when(predictions.predictPerformance(anyMap())).thenReturn(performance);
        when(predictions.predictPromotion(features)).thenReturn(new PromotionPredictionResponse());

        new RiskSummaryService(employees, predictions).summarize(7L);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(predictions).predictPerformance(captor.capture());
        assertEquals(Map.of("Age", 35), captor.getValue());
    }

    @Test
    void carriesAllContributingModelVersionsAndLimitationsIntoDecisionSupportResponse() {
        EmployeeAiService employees = mock(EmployeeAiService.class);
        MlPredictionService predictions = mock(MlPredictionService.class);
        Employee employee = mock(Employee.class);
        when(employees.getEmployee(7L)).thenReturn(employee);
        when(employees.assessRisk(7L)).thenReturn(new AttritionRiskResponse(7L, "yes", 0.7, "High Risk",
                "attrition-service-current", "IBM HR synthetic employee_data.csv", "legacy-artifact", List.of("not calibrated")));
        when(employees.toPredictionMap(employee)).thenReturn(Map.of("Age", 35));
        PerformancePredictionResponse performance = new PerformancePredictionResponse();
        performance.setPerformance_prediction(3);
        performance.setPerformance_probability(0.8);
        performance.setClass_probabilities(Map.of("3", 0.8, "4", 0.2));
        performance.setModel_version("performance-classifier-v1");
        performance.setLimitations(List.of("held-out metrics only"));
        when(predictions.predictPerformance(anyMap())).thenReturn(performance);
        PromotionPredictionResponse promotion = new PromotionPredictionResponse();
        promotion.setRule_score(0.75);
        promotion.setModel_version("promotion-readiness-rules-v1");
        promotion.setTarget_type("portfolio_demo_rule_target");
        promotion.setLimitations(List.of("not calibrated"));
        when(predictions.predictPromotion(Map.of("Age", 35))).thenReturn(promotion);

        RiskSummaryResponse response = new RiskSummaryService(employees, predictions).summarize(7L);

        assertEquals("attrition-service-current", response.modelVersions().get("attrition"));
        assertEquals("performance-classifier-v1", response.modelVersions().get("performance"));
        assertEquals("promotion-readiness-rules-v1", response.modelVersions().get("promotionReadiness"));
        assertTrue(response.limitations().contains("not calibrated"));
        assertTrue(response.limitations().contains("held-out metrics only"));
    }
}
