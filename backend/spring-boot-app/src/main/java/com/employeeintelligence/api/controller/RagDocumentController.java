package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.RagDocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag/documents")
public class RagDocumentController {

    private final RagDocumentService ragDocumentService;

    public RagDocumentController(
            RagDocumentService ragDocumentService) {

        this.ragDocumentService = ragDocumentService;
    }

    @PostMapping
    public Map<String, String> createDocument(
            @RequestParam String content,
            @RequestParam(required = false) String metadata) {

        ragDocumentService.createDocument(
                content,
                metadata
        );

        return Map.of(
                "message",
                "Document and embedding stored successfully"
        );
    }
    @GetMapping("/search")
public List<Map<String, Object>> search(
        @RequestParam String query,
        @RequestParam(defaultValue = "3") int limit) {

    return ragDocumentService.searchSimilarDocuments(
            query,
            limit
    );
}
}