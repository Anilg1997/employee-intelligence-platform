
package com.employeeintelligence.api.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OllamaEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(
            @Value("${langchain4j.ollama.embedding-model.base-url:http://localhost:11434}")
            String baseUrl,
            @Value("${langchain4j.ollama.embedding-model.model-name:nomic-embed-text}")
            String modelName) {

        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
