package com.employeeintelligence.api.agent;

import com.employeeintelligence.api.agent.tools.AttritionRiskTool;
import com.employeeintelligence.api.agent.tools.EmployeeTool;
import com.employeeintelligence.api.agent.tools.HrPolicyTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class EmployeeAiAgent {

    private final EmployeeAgent assistant;
    private final int maxToolCalls;
    private final Duration timeout;

    public EmployeeAiAgent(
            ChatModel chatModel,
            EmployeeTool employeeTool,
            AttritionRiskTool attritionRiskTool,
            HrPolicyTool hrPolicyTool, int maxToolCalls, Duration timeout) {

        this.assistant = AiServices.builder(EmployeeAgent.class)
                .chatModel(chatModel)
                .tools(
                        employeeTool,
                        attritionRiskTool,
                        hrPolicyTool
                )
                .build();
        this.maxToolCalls = maxToolCalls;
        this.timeout = timeout;
    }

    public String ask(String question) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            AgentExecutionBudget.start(maxToolCalls, timeout);
            try { return assistant.chat(question); }
            finally { AgentExecutionBudget.clear(); }
        }).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).join();
    }

    public interface EmployeeAgent {

        String chat(String message);
    }
}
