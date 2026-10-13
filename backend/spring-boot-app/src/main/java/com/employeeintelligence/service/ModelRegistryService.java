package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.ModelMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Reads metadata from JSON artifacts only; it never loads pickle or other model binaries. */
@Service
public class ModelRegistryService {
    private static final List<Artifact> ARTIFACTS = List.of(
            new Artifact("attrition", "employee_attrition_model.json"),
            new Artifact("salary", "employee_salary_model.json"),
            new Artifact("performance", "employee_performance_model.json"),
            new Artifact("promotion-readiness", "employee_promotion_model.json"));

    private final ObjectMapper objectMapper;
    private final Path modelsDirectory;

    @Autowired
    public ModelRegistryService(@Value("${ml.models.directory:}") String configuredDirectory) {
        this(new ObjectMapper(), resolveDirectory(configuredDirectory));
    }

    public ModelRegistryService(ObjectMapper objectMapper, Path modelsDirectory) {
        this.objectMapper = objectMapper;
        this.modelsDirectory = modelsDirectory;
    }

    public List<ModelMetadata> list() {
        return ARTIFACTS.stream().map(this::read).toList();
    }

    private ModelMetadata read(Artifact artifact) {
        try {
            JsonNode root = objectMapper.readTree(Files.readString(modelsDirectory.resolve(artifact.fileName())));
            JsonNode metadata = root == null ? null : root.get("metadata");
            if (metadata == null || !metadata.isObject()
                    || !isText(metadata, "model_version") || !isText(metadata, "algorithm")) {
                return unavailable(artifact.name());
            }
            Map<String, Object> metrics = new LinkedHashMap<>();
            JsonNode declaredMetrics = metadata.get("metrics");
            if (declaredMetrics != null && declaredMetrics.isObject()) {
                metrics.putAll(objectMapper.convertValue(declaredMetrics, Map.class));
            }
            JsonNode threshold = metadata.get("threshold");
            if (threshold == null) threshold = root.get("threshold");
            if (threshold != null && threshold.isNumber()) metrics.put("threshold", threshold.numberValue());
            Double thresholdValue = threshold != null && threshold.isNumber() ? threshold.doubleValue() : null;

            List<String> limitations = new ArrayList<>();
            JsonNode declaredLimitations = metadata.get("limitations");
            if (declaredLimitations != null && declaredLimitations.isArray()) {
                declaredLimitations.forEach(value -> { if (value.isTextual()) limitations.add(value.textValue()); });
            }
            String version = metadata.get("model_version").textValue();
            String status = isText(metadata, "status") ? metadata.get("status").textValue() : "AVAILABLE";
            return new ModelMetadata(artifact.name(), version,
                    metadata.get("algorithm").textValue(), status,
                    Collections.unmodifiableMap(metrics), List.copyOf(limitations),
                    textOrNull(metadata, "artifact_format"),
                    metadata.has("trained") && metadata.get("trained").isBoolean()
                            ? metadata.get("trained").booleanValue() : null,
                    textOrNull(metadata, "target_type"),
                    textOrNull(metadata, "dataset"),
                    textOrNull(metadata, "trained_at_utc"),
                    datasetVersion(metadata), thresholdValue,
                    textOrNull(metadata, "deployed_version"));
        } catch (IOException | RuntimeException exception) {
            return unavailable(artifact.name());
        }
    }

    private static ModelMetadata unavailable(String name) {
        return new ModelMetadata(name, null, null, "UNAVAILABLE", Map.of(),
                List.of("Artifact is unavailable or malformed."),
                null, null, null, null, null, null, null, null);
    }

    private static String textOrNull(JsonNode node, String field) {
        return isText(node, field) ? node.get(field).textValue() : null;
    }

    private static String datasetVersion(JsonNode metadata) {
        String explicit = textOrNull(metadata, "dataset_version");
        if (explicit != null) return explicit;
        String hash = textOrNull(metadata, "dataset_sha256");
        return hash == null ? null : "sha256:" + hash;
    }

    private static boolean isText(JsonNode node, String field) {
        return node.has(field) && node.get(field).isTextual() && !node.get(field).textValue().isBlank();
    }

    private static Path resolveDirectory(String configuredDirectory) {
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            try {
                return Path.of(configuredDirectory).toAbsolutePath().normalize();
            } catch (java.nio.file.InvalidPathException exception) {
                return null;
            }
        }
        Path workingDirectory = Path.of(System.getProperty("user.dir"));
        for (Path candidate : List.of(workingDirectory.resolve("ml-service/models"),
                workingDirectory.resolve("../ml-service/models"),
                workingDirectory.resolve("../../ml-service/models"))) {
            if (Files.isDirectory(candidate)) return candidate.toAbsolutePath().normalize();
        }
        return workingDirectory.resolve("ml-service/models").toAbsolutePath().normalize();
    }

    private record Artifact(String name, String fileName) {}
}
