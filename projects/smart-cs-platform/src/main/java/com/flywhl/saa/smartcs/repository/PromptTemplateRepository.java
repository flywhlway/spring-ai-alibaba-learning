package com.flywhl.saa.smartcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.PromptTemplateEntity;

/**
 * prompt_template JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, Long> {

    List<PromptTemplateEntity> findByTemplateKeyOrderByVersionDesc(String templateKey);

    Optional<PromptTemplateEntity> findTopByTemplateKeyOrderByVersionDesc(String templateKey);

    Optional<PromptTemplateEntity> findByTemplateKeyAndStatusOrderByVersionDesc(String templateKey, String status);

    Optional<PromptTemplateEntity> findFirstByTemplateKeyAndStatusOrderByVersionDesc(String templateKey, String status);

    List<PromptTemplateEntity> findByStatusOrderByTemplateKeyAsc(String status);
}
