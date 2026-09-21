package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.RagDocumentIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag/ingestion")
public class RagIngestionController {

    private final RagDocumentIngestionService ingestionService;

    public RagIngestionController(
            RagDocumentIngestionService ingestionService) {

        this.ingestionService = ingestionService;
    }

    @PostMapping
    public Map<String, String> ingest(
            @RequestParam String filePath,
            @RequestParam String source) throws Exception {

        ingestionService.ingestDocument(
                filePath,
                source
        );

        return Map.of(
                "message",
                "Document ingested successfully"
        );
    }
}