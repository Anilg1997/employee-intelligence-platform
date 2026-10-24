package com.employeeintelligence.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DlqReplayRequest(@NotBlank String deadLetterTopic, @NotBlank String payload, String key) { }
