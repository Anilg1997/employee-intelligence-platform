package com.employeeintelligence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be at most 2000 characters")
        String message) {
}
