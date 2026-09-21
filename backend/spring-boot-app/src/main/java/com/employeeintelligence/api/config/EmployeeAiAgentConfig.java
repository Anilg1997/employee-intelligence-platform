package com.employeeintelligence.api.config;

import com.employeeintelligence.api.agent.EmployeeAiAgent;
import com.employeeintelligence.api.agent.tools.AttritionRiskTool;
import com.employeeintelligence.api.agent.tools.EmployeeTool;
import com.employeeintelligence.api.agent.tools.HrPolicyTool;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeAiAgentConfig {

    @Bean
    public EmployeeAiAgent employeeAiAgent(
            ChatModel chatModel,
            EmployeeTool employeeTool,
            AttritionRiskTool attritionRiskTool,
            HrPolicyTool hrPolicyTool) {

        return new EmployeeAiAgent(
                chatModel,
                employeeTool,
                attritionRiskTool,
                hrPolicyTool
        );
    }
}