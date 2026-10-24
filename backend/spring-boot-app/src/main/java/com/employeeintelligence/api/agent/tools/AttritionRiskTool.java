package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.dto.AttritionRiskResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import com.employeeintelligence.api.agent.AgentToolSecurity;

@Component
public class AttritionRiskTool {

    private final EmployeeAiService employeeAiService;
    private final AgentToolSecurity security;

    public AttritionRiskTool(EmployeeAiService employeeAiService, AgentToolSecurity security) {
        this.employeeAiService = employeeAiService;
        this.security = security;
    }

    @Tool("Assess the employee attrition risk using the machine learning model")
    public AttritionRiskResponse assessAttritionRisk(Long employeeId) {

        security.check("assess_attrition_risk");
        var result = employeeAiService.assessRisk(employeeId);
        security.completed("assess_attrition_risk", "returned");
        return result;
    }
}
