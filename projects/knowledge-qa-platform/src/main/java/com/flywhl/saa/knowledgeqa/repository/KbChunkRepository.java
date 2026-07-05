package com.flywhl.saa.knowledgeqa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.knowledgeqa.model.entity.KbChunk;

/**
 * kb_chunk JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {

    List<KbChunk> findByDocumentIdOrderBySeqNoAsc(Long documentId);

    Optional<KbChunk> findByMilvusPk(String milvusPk);

    void deleteByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);
}
