package com.employeeintelligence.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.employeeintelligence.api.dto.ModelMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelRegistryServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void readsAllFourJsonRegistryEntriesWithoutSecrets() throws Exception {
        Path directory = Files.createTempDirectory("model-registry");
        write(directory, "employee_attrition_model.json", """
                {"metadata":{"model_version":"attrition-v1","algorithm":"logistic-regression","threshold":0.55,"artifact_format":"nexora-json-v1","limitations":["synthetic"]}}
                """);
        write(directory, "employee_salary_model.json", """
                {"metadata":{"model_version":"salary-v2","algorithm":"random-forest-regressor","metrics":{"test":{"mae":840.6}},"artifact_format":"nexora-json-v1"}}
                """);
        write(directory, "employee_performance_model.json", """
                {"metadata":{"model_version":"performance-v1","algorithm":"random-forest-classifier","metrics":{"test":{"accuracy":1.0}}}}
                """);
        write(directory, "employee_promotion_model.json", """
                {"metadata":{"model_version":"promotion-v1","algorithm":"deterministic-business-rules","trained":false,"target_type":"portfolio_demo_rule_target","limitations":["not ground truth"]}}
                """);

        List<ModelMetadata> models = new ModelRegistryService(mapper, directory).list();
        assertEquals(List.of("attrition", "salary", "performance", "promotion-readiness"), models.stream().map(ModelMetadata::name).toList());
        assertTrue(models.stream().allMatch(model -> "AVAILABLE".equals(model.status())));
        assertEquals(0.55, models.get(0).metrics().get("threshold"));
        assertEquals("nexora-json-v1", models.get(0).artifactFormat());
        String response = mapper.writeValueAsString(models);
        assertFalse(response.contains("dataset_sha256"));
        assertFalse(response.contains("source_artifact_sha256"));
        assertFalse(response.contains("hyperparameters"));
    }

    @Test
    void includesSalaryMetrics() throws Exception {
        Path directory = Files.createTempDirectory("model-registry-salary");
        write(directory, "employee_salary_model.json", "{" +
                "\"metadata\":{\"model_version\":\"salary-v2\",\"algorithm\":\"rf\",\"metrics\":{" +
                "\"test\":{\"mae\":840.6,\"r2\":0.93}}}}");
        ModelMetadata salary = new ModelRegistryService(mapper, directory).list().get(1);
        assertEquals(840.6, ((java.util.Map<?, ?>) salary.metrics().get("test")).get("mae"));
    }

    @Test
    void exposesPromotionTrainingAndLimitations() throws Exception {
        Path directory = Files.createTempDirectory("model-registry-promotion");
        write(directory, "employee_promotion_model.json", "{" +
                "\"metadata\":{\"model_version\":\"promotion-v1\",\"algorithm\":\"rules\",\"trained\":false," +
                "\"target_type\":\"portfolio_demo_rule_target\",\"limitations\":[\"demo only\"]}}");
        ModelMetadata promotion = new ModelRegistryService(mapper, directory).list().get(3);
        assertEquals(Boolean.FALSE, promotion.trained());
        assertEquals("portfolio_demo_rule_target", promotion.targetType());
        assertEquals(List.of("demo only"), promotion.limitations());
    }

    @Test
    void reportsMissingAndMalformedArtifactsAsUnavailable() throws Exception {
        Path directory = Files.createTempDirectory("model-registry-unavailable");
        write(directory, "employee_attrition_model.json", "not-json");
        List<ModelMetadata> models = new ModelRegistryService(mapper, directory).list();
        assertEquals("UNAVAILABLE", models.get(0).status());
        assertNull(models.get(0).version());
        assertEquals("UNAVAILABLE", models.get(1).status());
        assertEquals("UNAVAILABLE", models.get(3).status());
    }

    @Test
    void returnsStableAndDefensivelyImmutableMetadata() throws Exception {
        Path directory = Files.createTempDirectory("model-registry-immutable");
        write(directory, "employee_attrition_model.json", "{\"metadata\":{\"model_version\":\"v1\",\"algorithm\":\"lr\",\"metrics\":{\"z\":1,\"a\":2}}}");

        ModelMetadata metadata = new ModelRegistryService(mapper, directory).list().get(0);
        assertEquals(List.of("a", "z"), metadata.metrics().keySet().stream().toList());
        assertThrows(UnsupportedOperationException.class, () -> metadata.metrics().put("new", 1));
        assertThrows(UnsupportedOperationException.class, () -> metadata.limitations().add("new"));
    }

    private static void write(Path directory, String fileName, String contents) throws Exception {
        Files.writeString(directory.resolve(fileName), contents);
    }
}
