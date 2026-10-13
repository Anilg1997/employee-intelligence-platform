package com.employeeintelligence.api.service;

import com.employeeintelligence.security.TenantContext;
import dev.langchain4j.model.embedding.EmbeddingModel;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Tenant-scoped keyword/vector fusion. Retrieved text remains untrusted data. */
@Service
public class HybridRagRetrievalService {
    static final int MAX_RESULTS = 20;
    private static final String SEARCH_SQL = """
            WITH scored AS (
                SELECT
                    id,
                    content,
                    metadata,
                    COALESCE(1 - (embedding <=> ?::vector), 0) AS similarity,
                    LEAST(
                        ts_rank_cd(to_tsvector('simple', content), plainto_tsquery('simple', ?)),
                        1.0
                    ) AS keyword_score
                FROM rag_documents
                WHERE tenant_id = ?
            )
            SELECT
                id,
                content,
                metadata->>'source' AS source,
                similarity,
                keyword_score,
                (0.70 * GREATEST(similarity, 0) + 0.30 * keyword_score) AS fused_score
            FROM scored
            WHERE similarity >= ? OR keyword_score > 0
            ORDER BY fused_score DESC, similarity DESC, id ASC
            LIMIT ?
            """;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final TenantContext tenantContext;

    public HybridRagRetrievalService(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate,
                                     TenantContext tenantContext) {
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantContext = tenantContext;
    }

    public List<Map<String, Object>> search(String query, int limit, double minimumSimilarity) {
        RagService.validateQuestion(query);
        if (limit < 1 || limit > MAX_RESULTS) {
            throw new IllegalArgumentException("RAG result limit must be between 1 and " + MAX_RESULTS);
        }
        if (!Double.isFinite(minimumSimilarity) || minimumSimilarity < -1 || minimumSimilarity > 1) {
            throw new IllegalArgumentException("RAG minimum similarity must be between -1 and 1");
        }
        List<Float> vector = embeddingModel.embed(query).content().vectorAsList();
        String embedding = vector.toString();
        return jdbcTemplate.queryForList(
                SEARCH_SQL,
                embedding, query, tenantContext.currentTenantId(), minimumSimilarity, limit);
    }
}
