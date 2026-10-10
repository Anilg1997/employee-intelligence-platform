package com.employeeintelligence.api.dto;
import java.util.List; import java.util.Map;
public record ModelMetadata(String name, String version, String algorithm, String status, Map<String,Object> metrics, List<String> limitations) {}
