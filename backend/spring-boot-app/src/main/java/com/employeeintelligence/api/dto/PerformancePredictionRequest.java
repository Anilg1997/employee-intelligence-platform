package com.employeeintelligence.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict leakage-safe performance features; PerformanceRating is deliberately absent. */
public class PerformancePredictionRequest {
    private final Map<String, Object> features = new LinkedHashMap<>();
    @JsonAnySetter
    public void put(String name, Object value) { features.put(name, value); }
    public Map<String, Object> features() { return Map.copyOf(features); }
}
