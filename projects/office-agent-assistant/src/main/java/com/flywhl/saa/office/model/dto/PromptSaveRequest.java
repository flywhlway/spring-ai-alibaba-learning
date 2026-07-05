package com.flywhl.saa.office.model.dto;
import jakarta.validation.constraints.NotBlank;
public record PromptSaveRequest(@NotBlank String templateKey, @NotBlank String content, String description) {}

