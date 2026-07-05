package com.flywhl.saa.knowledgeqa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;

/**
 * kb_document JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {

    Page<KbDocument> findByStatusAndCategory(String status, String category, Pageable pageable);

    Page<KbDocument> findByStatus(String status, Pageable pageable);

    Page<KbDocument> findByCategory(String category, Pageable pageable);

    long countByStatus(String status);
}
