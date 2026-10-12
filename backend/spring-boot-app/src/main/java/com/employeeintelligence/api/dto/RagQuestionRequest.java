package com.employeeintelligence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Bounded user input for policy retrieval; retrieved documents remain untrusted data. */
public record RagQuestionRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 2000, message = "Question must be at most 2000 characters")
        String question) {
}
