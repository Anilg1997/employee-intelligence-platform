package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RagController.class)
@Import(com.employeeintelligence.api.config.SecurityConfig.class)
class RagControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private RagService ragService;

    @Test
    void rejectsBlankQuestionBeforeCallingRagService() throws Exception {
        mockMvc.perform(post("/api/rag/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
        verifyNoInteractions(ragService);
    }

    @Test
    void rejectsOversizedQuestionBeforeCallingRagService() throws Exception {
        mockMvc.perform(post("/api/rag/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"" + "x".repeat(2001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
        verifyNoInteractions(ragService);
    }
}
