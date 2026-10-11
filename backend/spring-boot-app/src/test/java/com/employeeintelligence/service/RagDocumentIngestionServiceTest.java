package com.employeeintelligence.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;

class RagDocumentIngestionServiceTest {

    @Test
    void rejectsPathTraversalOutsideTrustedDirectory() throws Exception {
        Path trustedDirectory = Files.createTempDirectory("rag-documents");
        RagDocumentIngestionService service = new RagDocumentIngestionService(
                mock(EmbeddingModel.class),
                mock(JdbcTemplate.class),
                trustedDirectory.toString(),
                1024,
                "txt,md");

        assertThrows(IllegalArgumentException.class,
                () -> service.ingestDocument("../outside.txt", "test"));
    }

    @Test
    void rejectsUnsupportedExtension() throws Exception {
        Path trustedDirectory = Files.createTempDirectory("rag-documents");
        Path document = Files.writeString(trustedDirectory.resolve("document.exe"), "content");
        RagDocumentIngestionService service = new RagDocumentIngestionService(
                mock(EmbeddingModel.class),
                mock(JdbcTemplate.class),
                trustedDirectory.toString(),
                1024,
                "txt,md");

        assertThrows(IllegalArgumentException.class,
                () -> service.ingestDocument(document.toString(), "test"));
    }

    @Test
    void serializesMalformedSourceAsValidJsonMetadata() throws Exception {
        Path trustedDirectory = Files.createTempDirectory("rag-documents");
        Path document = Files.writeString(trustedDirectory.resolve("document.txt"), "content");
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        Embedding embedding = mock(Embedding.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        String source = "quarterly\\\"report\n\tfinal";
        when(embedding.vectorAsList()).thenReturn(List.of(0.1f));
        when(embeddingModel.embed("content")).thenReturn(Response.from(embedding));
        RagDocumentIngestionService service = new RagDocumentIngestionService(
                embeddingModel,
                jdbcTemplate,
                trustedDirectory.toString(),
                1024,
                "txt,md");

        service.ingestDocument(document.toString(), source);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(anyString(), eq("content"), metadataCaptor.capture(), eq("[0.1]"));
        JsonNode metadata = new ObjectMapper().readTree(metadataCaptor.getValue());
        assertEquals(source, metadata.get("source").asText());
    }
}
