package com.flywhl.saa.office.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flywhl.saa.office.model.entity.PromptTemplateEntity;
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, Long> {
    List<PromptTemplateEntity> findByTemplateKeyOrderByVersionDesc(String templateKey);
    Optional<PromptTemplateEntity> findFirstByTemplateKeyAndStatusOrderByVersionDesc(String templateKey, String status);
    Optional<PromptTemplateEntity> findTopByTemplateKeyOrderByVersionDesc(String templateKey);
    List<PromptTemplateEntity> findByStatusOrderByTemplateKeyAsc(String status);
}

