package com.flywhl.saa.office.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_preference", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pref_key"}))
@Getter @Setter
public class UserPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "pref_key", nullable = false, length = 64)
    private String prefKey;
    @Column(name = "pref_value", nullable = false, columnDefinition = "TEXT")
    private String prefValue;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

