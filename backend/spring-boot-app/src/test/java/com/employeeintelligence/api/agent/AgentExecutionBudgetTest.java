package com.employeeintelligence.api.agent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AgentExecutionBudgetTest {
    @AfterEach void cleanup() { AgentExecutionBudget.clear(); }
    @Test void rejectsToolCallsAfterConfiguredBudget() {
        AgentExecutionBudget budget = AgentExecutionBudget.start(1, Duration.ofSeconds(1));
        budget.consumeToolCall();
        assertEquals(1, budget.toolCalls());
        assertThrows(IllegalStateException.class, budget::consumeToolCall);
    }
}
