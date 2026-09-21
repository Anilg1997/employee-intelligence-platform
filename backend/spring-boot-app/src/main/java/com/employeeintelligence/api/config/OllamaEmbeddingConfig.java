package com.employeeintelligence.api.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaEmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {

        System.setProperty(
                "langchain4j.http.clientBuilderFactory",
                "dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory"
        );

        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();
    }
}