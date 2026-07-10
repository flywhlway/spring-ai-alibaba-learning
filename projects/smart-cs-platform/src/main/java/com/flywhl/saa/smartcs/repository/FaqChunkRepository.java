package com.flywhl.saa.smartcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.smartcs.model.entity.FaqChunk;

/**
 * faq_chunk JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface FaqChunkRepository extends JpaRepository<FaqChunk, Long> {

    List<FaqChunk> findByArticleId(Long articleId);

    @Modifying
    @Transactional
    void deleteByArticleId(Long articleId);
}
