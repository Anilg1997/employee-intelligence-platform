package com.employeeintelligence.api.agent;

import com.employeeintelligence.api.agent.tools.AttritionRiskTool;
import com.employeeintelligence.api.agent.tools.EmployeeTool;
import com.employeeintelligence.api.agent.tools.HrPolicyTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

public class EmployeeAiAgent {

    private final EmployeeAgent assistant;

    public EmployeeAiAgent(
            ChatModel chatModel,
            EmployeeTool employeeTool,
            AttritionRiskTool attritionRiskTool,
            HrPolicyTool hrPolicyTool) {

        this.assistant = AiServices.builder(EmployeeAgent.class)
                .chatModel(chatModel)
                .tools(
                        employeeTool,
                        attritionRiskTool,
                        hrPolicyTool
                )
                .build();
    }

    public String ask(String question) {
        return assistant.chat(question);
    }

    public interface EmployeeAgent {

        String chat(String message);
    }
}