package com.employeeintelligence.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import dev.langchain4j.model.chat.ChatModel;
import com.employeeintelligence.api.agent.EmployeeAiAgent;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(EmployeeIntelligenceApiApplicationTests.TestChatModelConfig.class)
class EmployeeIntelligenceApiApplicationTests {

    @TestConfiguration
    static class TestChatModelConfig {
        @Bean
        ChatModel chatModel() {
            return mock(ChatModel.class);
        }

        @Bean
        EmployeeAiAgent employeeAiAgent() {
            return mock(EmployeeAiAgent.class);
        }
    }

	@Test
	void contextLoads() {
	}

}
