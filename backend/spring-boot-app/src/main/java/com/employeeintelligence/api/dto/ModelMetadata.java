package com.employeeintelligence.api.dto;

import java.util.List;
import java.util.Map;

/** Public metadata for one model artifact. Optional fields are null when undeclared. */
public record ModelMetadata(String name, String version, String algorithm, String status,
                            Map<String, Object> metrics, List<String> limitations,
                            String artifactFormat, Boolean trained, String targetType) {
    public ModelMetadata(String name, String version, String algorithm, String status,
                         Map<String, Object> metrics, List<String> limitations) {
        this(name, version, algorithm, status, metrics, limitations, null, null, null);
    }
}
