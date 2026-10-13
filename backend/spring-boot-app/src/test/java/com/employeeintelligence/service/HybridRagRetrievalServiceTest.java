package com.employeeintelligence.api.service;

import com.employeeintelligence.security.TenantContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HybridRagRetrievalServiceTest {
    @Test
    void combinesParameterizedKeywordAndVectorScoresWithinTheCurrentTenant() {
        EmbeddingModel embeddings = mock(EmbeddingModel.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Embedding embedding = mock(Embedding.class);
        when(embedding.vectorAsList()).thenReturn(List.of(0.1f, 0.2f));
        when(embeddings.embed("overtime policy")).thenReturn(Response.from(embedding));
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(Map.of(
                "source", "overtime.md", "similarity", 0.81, "keyword_score", 0.5,
                "fused_score", 0.717, "content", "Overtime requires approval.")));

        new HybridRagRetrievalService(embeddings, jdbc, new TenantContext(false))
                .search("overtime policy", 3, 0.75);

        verify(jdbc).queryForList(
                org.mockito.ArgumentMatchers.argThat(sql -> sql.contains("plainto_tsquery")
                        && sql.contains("tenant_id = ?")
                        && sql.contains("fused_score")),
                any(Object[].class));
    }

    @Test
    void rejectsInvalidLimitsBeforeEmbedding() {
        EmbeddingModel embeddings = mock(EmbeddingModel.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        HybridRagRetrievalService retrieval = new HybridRagRetrievalService(
                embeddings, jdbc, new TenantContext(false));

        assertThrows(IllegalArgumentException.class, () -> retrieval.search("policy", 0, 0.75));
        assertThrows(IllegalArgumentException.class, () -> retrieval.search("policy", 21, 0.75));
        assertThrows(IllegalArgumentException.class, () -> retrieval.search("policy", 3, Double.NaN));
    }
}
