package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.EmbeddingTestService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingTestController {

    private final EmbeddingTestService embeddingTestService;

    public EmbeddingTestController(
            EmbeddingTestService embeddingTestService) {
        this.embeddingTestService = embeddingTestService;
    }

    @PostMapping("/test")
    public Map<String, Object> test(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");

        int dimensions =
                embeddingTestService.generateEmbedding(text);

        return Map.of(
                "dimensions", dimensions,
                "message", "Embedding generated successfully"
        );
    }
}