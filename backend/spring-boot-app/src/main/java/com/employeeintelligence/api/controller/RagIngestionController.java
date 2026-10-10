package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.RagDocumentIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/rag/ingestion")
public class RagIngestionController {

    private final RagDocumentIngestionService ingestionService;

    public RagIngestionController(
            RagDocumentIngestionService ingestionService) {

        this.ingestionService = ingestionService;
    }

    @PostMapping
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
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
