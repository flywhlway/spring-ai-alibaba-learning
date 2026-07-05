package com.flywhl.saa.knowledgeqa.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.rag.DocumentEtlPipeline;
import com.flywhl.saa.knowledgeqa.rag.IngestStatusTracker;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;

/**
 * 启动时补齐演示知识向量：data.sql 中 INDEXED 但无 kb_chunk / Milvus 向量的文档触发 reindex。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class DemoKnowledgeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoKnowledgeSeeder.class);

    private final KbDocumentRepository documentRepository;
    private final KbChunkRepository chunkRepository;
    private final DocumentEtlPipeline documentEtlPipeline;

    public DemoKnowledgeSeeder(
            KbDocumentRepository documentRepository,
            KbChunkRepository chunkRepository,
            DocumentEtlPipeline documentEtlPipeline) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.documentEtlPipeline = documentEtlPipeline;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<KbDocument> candidates = documentRepository
                .findByStatus(IngestStatusTracker.STATUS_INDEXED, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(this::needsReindex)
                .toList();

        if (candidates.isEmpty()) {
            log.debug("演示知识向量已就绪，无需 Seeder 补齐");
            return;
        }

        log.info("检测到 {} 条演示文档缺少向量索引，触发 reindex", candidates.size());
        for (KbDocument document : candidates) {
            documentEtlPipeline.reindex(document.getId());
        }
    }

    private boolean needsReindex(KbDocument document) {
        if (document.getChunkCount() == null || document.getChunkCount() == 0) {
            return true;
        }
        return chunkRepository.countByDocumentId(document.getId()) == 0;
    }
}
