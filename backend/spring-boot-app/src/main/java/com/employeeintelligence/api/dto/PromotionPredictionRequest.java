package com.employeeintelligence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** The performance feature contract plus the promotion rule's observed rating. */
public class PromotionPredictionRequest extends PerformancePredictionRequest {
    @NotNull @Min(1) @Max(4) @JsonProperty("PerformanceRating")
    private Integer performanceRating;

    @Override
    public java.util.Map<String, Object> features() {
        var values = new java.util.LinkedHashMap<>(super.features());
        values.put("PerformanceRating", performanceRating);
        return values;
    }
}
