package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.RagDocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/rag/documents")
public class RagDocumentController {

    private final RagDocumentService ragDocumentService;

    public RagDocumentController(
            RagDocumentService ragDocumentService) {

        this.ragDocumentService = ragDocumentService;
    }

    @PostMapping
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN')")
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
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
public List<Map<String, Object>> search(
        @RequestParam String query,
        @RequestParam(defaultValue = "3") int limit) {

    return ragDocumentService.searchSimilarDocuments(
            query,
            limit
    );
}
}
