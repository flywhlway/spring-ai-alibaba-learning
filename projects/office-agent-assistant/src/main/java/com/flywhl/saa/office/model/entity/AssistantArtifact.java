package com.flywhl.saa.office.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "assistant_artifact")
@Getter @Setter
public class AssistantArtifact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 32)
    private String type;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
    @Column(name = "file_path", length = 512)
    private String filePath;
    @Column(length = 64)
    private String model;
    @Column(name = "input_tokens", nullable = false)
    private Integer inputTokens = 0;
    @Column(name = "output_tokens", nullable = false)
    private Integer outputTokens = 0;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

