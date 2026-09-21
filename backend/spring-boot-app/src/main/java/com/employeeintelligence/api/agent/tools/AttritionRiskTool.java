package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.dto.AttritionRiskResponse;
import com.employeeintelligence.api.service.EmployeeAiService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class AttritionRiskTool {

    private final EmployeeAiService employeeAiService;

    public AttritionRiskTool(EmployeeAiService employeeAiService) {
        this.employeeAiService = employeeAiService;
    }

    @Tool("Assess the employee attrition risk using the machine learning model")
    public AttritionRiskResponse assessAttritionRisk(Long employeeId) {

        return employeeAiService.assessRisk(employeeId);
    }
}