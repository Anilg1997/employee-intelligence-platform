package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.AiChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public Map<String, String> chat(
            @RequestBody Map<String, String> request) {

        String message = request.get("message");

        String response = aiChatService.chat(message);

        return Map.of(
                "message", message,
                "response", response
        );
    }
}
