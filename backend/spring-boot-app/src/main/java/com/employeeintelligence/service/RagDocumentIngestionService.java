package com.employeeintelligence.api.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Service
public class RagDocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final Path trustedDirectory;
    private final long maxFileSizeBytes;
    private final Set<String> allowedExtensions;

    public RagDocumentIngestionService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate,
            @Value("${rag.ingestion.directory}") String trustedDirectory,
            @Value("${rag.ingestion.max-file-size-bytes:10485760}") long maxFileSizeBytes,
            @Value("${rag.ingestion.allowed-extensions:txt,md,csv,json}") String allowedExtensions) {

        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.trustedDirectory = Path.of(trustedDirectory).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedExtensions = Arrays.stream(allowedExtensions.split(","))
                .map(extension -> extension.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\.", ""))
                .filter(extension -> !extension.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public void ingestDocument(
            String filePath,
            String source) throws Exception {

        // Resolve and validate before reading so callers cannot escape the trusted directory.
        Path requestedPath = Path.of(filePath);
        Path documentPath = (requestedPath.isAbsolute()
                ? requestedPath
                : trustedDirectory.resolve(requestedPath))
                .toAbsolutePath().normalize();
        if (!Files.isDirectory(trustedDirectory)) {
            throw new IllegalArgumentException("Configured ingestion directory does not exist");
        }
        Path trustedRealDirectory = trustedDirectory.toRealPath();
        if (!documentPath.startsWith(trustedRealDirectory)) {
            throw new IllegalArgumentException("Document path must be inside the configured ingestion directory");
        }
        if (!Files.isRegularFile(documentPath)) {
            throw new IllegalArgumentException("Document does not exist or is not a regular file");
        }
        documentPath = documentPath.toRealPath();
        if (!documentPath.startsWith(trustedRealDirectory)) {
            throw new IllegalArgumentException("Document path must be inside the configured ingestion directory");
        }
        if (Files.size(documentPath) > maxFileSizeBytes) {
            throw new IllegalArgumentException("Document exceeds the configured maximum size");
        }
        String fileName = documentPath.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.') + 1;
        String extension = extensionStart > 0 ? fileName.substring(extensionStart).toLowerCase(Locale.ROOT) : "";
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Document extension is not allowed");
        }

        // 1. Read the validated document
        String content = Files.readString(documentPath);

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
