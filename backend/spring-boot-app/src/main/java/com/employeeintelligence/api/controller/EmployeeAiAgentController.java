
package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.agent.EmployeeAiAgent;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class EmployeeAiAgentController {

    private final EmployeeAiAgent employeeAiAgent;

    public EmployeeAiAgentController(EmployeeAiAgent employeeAiAgent) {
        this.employeeAiAgent = employeeAiAgent;
    }

    @PostMapping("/ask")
    @PreAuthorize("@securityMode.isDemoMode() or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public Map<String, String> ask(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "The question field is required."
            );
        }

        String answer = employeeAiAgent.ask(question);

        return Map.of(
                "question", question,
                "answer", answer
        );
    }
}
