package com.flywhl.saa.office.model.dto;
import jakarta.validation.constraints.NotBlank;
public record UserCreateRequest(@NotBlank String username, @NotBlank String password,
        @NotBlank String displayName, @NotBlank String role, String department, String email) {}

