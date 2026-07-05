package com.flywhl.saa.office.model.dto;
import jakarta.validation.constraints.NotBlank;
public record ChatRequest(@NotBlank String message, String conversationId) {}

