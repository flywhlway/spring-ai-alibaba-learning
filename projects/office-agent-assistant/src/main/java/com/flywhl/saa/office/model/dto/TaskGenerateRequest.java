package com.flywhl.saa.office.model.dto;
import jakarta.validation.constraints.NotBlank;
public record TaskGenerateRequest(@NotBlank String input, String title) {}

