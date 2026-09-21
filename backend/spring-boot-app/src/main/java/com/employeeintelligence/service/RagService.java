package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.RagResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ChatModel chatModel;

    public RagService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate,
            ChatModel chatModel) {

        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.chatModel = chatModel;
    }

    public RagResponse ask(String question) {

        // 1. Convert question into an embedding
        List<Float> vector = embeddingModel
                .embed(question)
                .content()
                .vectorAsList();

        String embedding = vector.toString();

        // 2. Find the most relevant documents
        String sql = """
                SELECT
                    content,
                    metadata->>'source' AS source,
                    1 - (embedding <=> ?::vector) AS similarity
                FROM rag_documents
                WHERE embedding IS NOT NULL
                ORDER BY embedding <=> ?::vector
                LIMIT 3
                """;

        List<Map<String, Object>> documents =
                jdbcTemplate.queryForList(
                        sql,
                        embedding,
                        embedding
                );

        // 3. Build context from retrieved documents
        StringBuilder context = new StringBuilder();

        for (Map<String, Object> document : documents) {
            context.append(document.get("content"))
                    .append("\n");
        }

        // 4. Build source information
       List<Map<String, Object>> sources = documents.stream()
        .map(document -> Map.of(
                "source", document.get("source"),
                "similarity", document.get("similarity"),
                "content", document.get("content")
        ))
        .toList();
        // 5. Build grounded prompt
       String prompt = """
        You are an HR assistant.

        Your answer MUST be based only on the retrieved HR context.

        IMPORTANT:
        - Preserve all important facts from relevant context.
        - Do not remove or shorten important facts.
        - Do not introduce information that is not present in the context.
        - Combine relevant statements when necessary.
        - If the context directly answers the question, answer directly.
        - If the context does not contain enough information, say:
          "The available HR documents do not contain enough information to answer this question."

        Retrieved HR context:
        %s

        User question:
        %s

        Provide a concise answer that includes all relevant information
        explicitly stated in the retrieved context.
        """.formatted(
        context,
        question
);

        // 6. Send prompt to Llama 3.2
        String answer = chatModel.chat(prompt);

        // 7. Return answer + sources
        return new RagResponse(
                question,
                answer,
                sources
        );
    }
}