package com.employeeintelligence.api.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
