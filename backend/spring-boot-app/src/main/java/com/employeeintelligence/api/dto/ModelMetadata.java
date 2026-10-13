package com.employeeintelligence.api.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Public metadata for one model artifact. Optional fields are null when undeclared. */
public record ModelMetadata(String name, String version, String algorithm, String status,
                            Map<String, Object> metrics, List<String> limitations,
                            String artifactFormat, Boolean trained, String targetType,
                            String dataset, String trainedAtUtc, String datasetVersion,
                            Double threshold, String deployedVersion) {
    public ModelMetadata {
        metrics = immutableMap(metrics);
        limitations = limitations == null ? List.of() : List.copyOf(limitations);
    }

    public ModelMetadata(String name, String version, String algorithm, String status,
                         Map<String, Object> metrics, List<String> limitations) {
        this(name, version, algorithm, status, metrics, limitations,
                null, null, null, null, null, null, null, null);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) return Map.of();
        Map<String, Object> copy = new LinkedHashMap<>();
        new java.util.TreeMap<>(source).forEach((key, value) -> copy.put(key, immutableValue(value)));
        return java.util.Collections.unmodifiableMap(copy);
    }

    private static Object immutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new java.util.TreeMap<>();
            map.forEach((key, nested) -> copy.put(String.valueOf(key), immutableValue(nested)));
            return java.util.Collections.unmodifiableMap(copy);
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            list.forEach(item -> copy.add(immutableValue(item)));
            return java.util.Collections.unmodifiableList(copy);
        }
        return value;
    }
}
