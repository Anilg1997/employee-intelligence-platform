package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.dto.RagResponse;
import com.employeeintelligence.api.service.RagService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class HrPolicyTool {

    private final RagService ragService;

    public HrPolicyTool(RagService ragService) {
        this.ragService = ragService;
    }

    @Tool("Search the company's HR policies and return relevant policy information")
    public RagResponse searchHrPolicy(String question) {

        return ragService.ask(question);
    }
}