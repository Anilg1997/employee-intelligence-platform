package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.dto.RagResponse;
import com.employeeintelligence.api.service.RagService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import com.employeeintelligence.api.agent.AgentToolSecurity;

@Component
public class HrPolicyTool {

    private final RagService ragService;
    private final AgentToolSecurity security;

    public HrPolicyTool(RagService ragService, AgentToolSecurity security) {
        this.ragService = ragService;
        this.security = security;
    }

    @Tool("Search the company's HR policies and return relevant policy information")
    public RagResponse searchHrPolicy(String question) {

        security.check("search_hr_policy");
        var result = ragService.ask(question);
        security.completed("search_hr_policy", "returned");
        return result;
    }
}
