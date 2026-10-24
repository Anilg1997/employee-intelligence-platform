package com.employeeintelligence.service;

import com.employeeintelligence.security.TenantContext;
import com.employeeintelligence.api.service.RagService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RagServiceTest {

    @Test
    void reportsMissingPolicyContextWithoutCallingTheLanguageModel() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        Embedding embedding = mock(Embedding.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<ChatModel> chatModels = mock(ObjectProvider.class);
        when(embedding.vectorAsList()).thenReturn(List.of(0.1f));
        when(embeddingModel.embed("leave policy")).thenReturn(Response.from(embedding));
        when(jdbcTemplate.queryForList(anyString(), any(), any(), any())).thenReturn(List.of());

        RagService service = new RagService(
                embeddingModel, jdbcTemplate, chatModels, new TenantContext(false));

        var response = service.ask("leave policy");

        assertEquals("The available HR documents do not contain enough information to answer this question.",
                response.getAnswer());
        assertEquals(List.of(), response.getSources());
        verifyNoInteractions(chatModels);
    }
}
