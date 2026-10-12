package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.AiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiChatController.class)
@Import(com.employeeintelligence.api.config.SecurityConfig.class)
class AiChatControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AiChatService aiChatService;

    @Test
    void rejectsBlankMessageBeforeCallingAiService() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType("application/json")
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
        verifyNoInteractions(aiChatService);
    }
}
