package com.flywhl.saa.knowledgeqa.rag;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;

/**
 * 入库状态跟踪：kb_document 状态机（UPLOADED→PARSING→INDEXED/FAILED）流转与失败原因记录。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class IngestStatusTracker {

    public static final String STATUS_UPLOADED = "UPLOADED";
    public static final String STATUS_PARSING = "PARSING";
    public static final String STATUS_INDEXED = "INDEXED";
    public static final String STATUS_FAILED = "FAILED";

    private final KbDocumentRepository documentRepository;

    public IngestStatusTracker(KbDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void markParsing(Long documentId) {
        updateStatus(documentId, STATUS_PARSING, null, null);
    }

    @Transactional
    public void markIndexed(Long documentId, int chunkCount) {
        updateStatus(documentId, STATUS_INDEXED, chunkCount, null);
    }

    @Transactional
    public void markFailed(Long documentId, String failReason) {
        updateStatus(documentId, STATUS_FAILED, 0, truncateReason(failReason));
    }

    @Transactional(readOnly = true)
    public String getStatus(Long documentId) {
        return documentRepository.findById(documentId)
                .map(KbDocument::getStatus)
                .orElse(null);
    }

    private void updateStatus(Long documentId, String status, Integer chunkCount, String failReason) {
        KbDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));
        document.setStatus(status);
        if (chunkCount != null) {
            document.setChunkCount(chunkCount);
        }
        document.setFailReason(failReason);
        document.setUpdatedAt(OffsetDateTime.now());
        documentRepository.save(document);
    }

    private static String truncateReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= 512 ? reason : reason.substring(0, 512);
    }
}
