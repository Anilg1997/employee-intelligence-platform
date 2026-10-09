package com.employeeintelligence.api.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

@Configuration
public class OllamaEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(
            @Value("${langchain4j.ollama.embedding-model.base-url:${OLLAMA_BASE_URL:http://localhost:11434}") String baseUrl,
            @Value("${langchain4j.ollama.embedding-model.model-name:${OLLAMA_EMBEDDING_MODEL:nomic-embed-text}") String modelName) {

        System.setProperty(
                "langchain4j.http.clientBuilderFactory",
                "dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory"
        );

        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
