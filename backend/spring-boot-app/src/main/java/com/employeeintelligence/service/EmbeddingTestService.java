package com.employeeintelligence.api.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingTestService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingTestService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public int generateEmbedding(String text) {
        return embeddingModel.embed(text)
                .content()
                .vectorAsList()
                .size();
    }
}