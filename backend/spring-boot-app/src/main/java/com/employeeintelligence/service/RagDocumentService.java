package com.employeeintelligence.api.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.employeeintelligence.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class RagDocumentService {

    private final dev.langchain4j.model.embedding.EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final TenantContext tenantContext;
    private final HybridRagRetrievalService retrieval;

    @Autowired
    public RagDocumentService(
            dev.langchain4j.model.embedding.EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate, TenantContext tenantContext,
            HybridRagRetrievalService retrieval) {

        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantContext = tenantContext;
        this.retrieval = retrieval;
    }

    public void createDocument(
            String content,
            String metadata) {

        List<Float> vector = embeddingModel
                .embed(content)
                .content()
                .vectorAsList();

        String embedding =
                vector.toString()
                        .replace("[", "[")
                        .replace("]", "]");

        String sql = """
                INSERT INTO rag_documents
                    (tenant_id, content, metadata, embedding)
                VALUES
                    (?, ?, ?::jsonb, ?::vector)
                """;

        jdbcTemplate.update(
                sql,
                tenantContext.currentTenantId(), content,
                metadata,
                embedding
        );
    }
    public List<Map<String, Object>> searchSimilarDocuments(String query, int limit) {
        return retrieval.search(query, limit, 0.0);
    }
}
