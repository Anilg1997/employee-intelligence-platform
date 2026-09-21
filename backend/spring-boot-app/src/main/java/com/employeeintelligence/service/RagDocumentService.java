package com.employeeintelligence.api.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagDocumentService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public RagDocumentService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate) {

        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
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
                    (content, metadata, embedding)
                VALUES
                    (?, ?::jsonb, ?::vector)
                """;

        jdbcTemplate.update(
                sql,
                content,
                metadata,
                embedding
        );
    }
    public List<Map<String, Object>> searchSimilarDocuments(
        String query,
        int limit) {

    List<Float> vector = embeddingModel
            .embed(query)
            .content()
            .vectorAsList();

    String embedding = vector.toString();

    String sql = """
            SELECT
                id,
                content,
                metadata,
                1 - (embedding <=> ?::vector) AS similarity
            FROM rag_documents
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

    return jdbcTemplate.queryForList(
            sql,
            embedding,
            embedding,
            limit
    );
}
}