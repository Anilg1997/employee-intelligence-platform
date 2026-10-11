package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.RagResponse;
import com.employeeintelligence.api.service.RagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public RagResponse ask(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        return ragService.ask(question);
    }
}
