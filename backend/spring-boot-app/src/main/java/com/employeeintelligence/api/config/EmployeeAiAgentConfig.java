package com.employeeintelligence.api.config;

import com.employeeintelligence.api.agent.EmployeeAiAgent;
import com.employeeintelligence.api.agent.tools.AttritionRiskTool;
import com.employeeintelligence.api.agent.tools.EmployeeTool;
import com.employeeintelligence.api.agent.tools.HrPolicyTool;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;

@Configuration
public class EmployeeAiAgentConfig {

    @Bean
    public EmployeeAiAgent employeeAiAgent(
            ChatModel chatModel,
            EmployeeTool employeeTool,
            AttritionRiskTool attritionRiskTool,
            HrPolicyTool hrPolicyTool,
            @Value("${agent.execution.max-tool-calls:8}") int maxToolCalls,
            @Value("${agent.execution.timeout-seconds:30}") long timeoutSeconds) {

        return new EmployeeAiAgent(
                chatModel,
                employeeTool,
                attritionRiskTool,
                hrPolicyTool, maxToolCalls, Duration.ofSeconds(Math.max(1, Math.min(timeoutSeconds, 120)))
        );
    }
}
