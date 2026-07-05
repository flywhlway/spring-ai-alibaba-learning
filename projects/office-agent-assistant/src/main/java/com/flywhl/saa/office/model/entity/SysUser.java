package com.flywhl.saa.office.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_user")
@Getter @Setter
public class SysUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;
    @Column(nullable = false, length = 32)
    private String role = "EMPLOYEE";
    @Column(length = 64)
    private String department;
    @Column(length = 128)
    private String email;
    @Column(nullable = false)
    private Boolean enabled = true;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

