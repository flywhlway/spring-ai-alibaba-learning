package com.flywhl.saa.smartcs.model.entity;

import java.time.OffsetDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 模型配置实体（表 model_profile）：场景化路由（FAQ/BUSINESS/TICKET），发布时同步推
 * Nacos {@code scs.model.profiles}。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Entity
@Table(name = "model_profile")
@Getter
@Setter
public class ModelProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_key", nullable = false, unique = true, length = 128)
    private String profileKey;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "model_name", nullable = false, length = 64)
    private String modelName;

    @Column(nullable = false, length = 32)
    private String scene;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false)
    private Boolean enabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private Map<String, Object> optionsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
