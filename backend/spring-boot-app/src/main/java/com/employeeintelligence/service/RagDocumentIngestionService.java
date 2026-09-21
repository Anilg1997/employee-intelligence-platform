package com.employeeintelligence.api.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class RagDocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public RagDocumentIngestionService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate) {

        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ingestDocument(
            String filePath,
            String source) throws Exception {

        // 1. Read the document
        String content = Files.readString(Path.of(filePath));

        // 2. Split document into paragraph-based chunks
        List<String> chunks = List.of(
                content.split("\\n\\s*\\n")
        );

        // 3. Clean chunks
        List<String> cleanedChunks = chunks.stream()
                .map(chunk -> chunk.replace("\r", "").trim())
                .filter(chunk -> !chunk.isBlank())
                .toList();

        // 4. Generate embedding and store each chunk
        for (String chunk : cleanedChunks) {

            List<Float> vector = embeddingModel
                    .embed(chunk)
                    .content()
                    .vectorAsList();

            String embedding = vector.toString();

            String metadata = """
                    {"source":"%s"}
                    """.formatted(source).trim();

            String sql = """
                    INSERT INTO rag_documents
                        (content, metadata, embedding)
                    VALUES
                        (?, ?::jsonb, ?::vector)
                    """;

            jdbcTemplate.update(
                    sql,
                    chunk,
                    metadata,
                    embedding
            );
        }
    }
}