package com.flywhl.saa.office.model.vo;
import java.time.LocalDateTime;
public record UserVO(Long id, String username, String displayName, String role,
        String department, String email, Boolean enabled, LocalDateTime createdAt) {}

