package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.RagResponse;
import com.employeeintelligence.api.service.RagService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public RagResponse ask(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        return ragService.ask(question);
    }
}