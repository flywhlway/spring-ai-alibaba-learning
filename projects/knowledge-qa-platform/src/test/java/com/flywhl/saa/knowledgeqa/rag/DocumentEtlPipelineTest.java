package com.flywhl.saa.knowledgeqa.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.model.entity.KbChunk;
import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;

import io.minio.MinioClient;

/**
 * DocumentEtlPipeline 单元测试：删除索引时状态与向量清理。
 *
 * @author flywhl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文档 ETL 流水线")
class DocumentEtlPipelineTest {

    @Mock
    private KbDocumentRepository documentRepository;
    @Mock
    private KbChunkRepository chunkRepository;
    @Mock
    private MinioClient minioClient;
    @Mock
    private VectorStore vectorStore;
    @Mock
    private IngestStatusTracker statusTracker;
    @Mock
    private DocumentEtlPipeline self;

    private DocumentEtlPipeline pipeline;

    @BeforeEach
    void setUp() {
        KqaProperties properties = new KqaProperties(
                new KqaProperties.Minio("http://localhost:9000", "minio", "minio", "kqa-documents"),
                new KqaProperties.Rag(5, 0.35, 512, 64, true),
                null,
                null);
        pipeline = new DocumentEtlPipeline(
                documentRepository, chunkRepository, minioClient, properties, vectorStore, statusTracker, self);
    }

    @Test
    @DisplayName("deleteIndex 清理 Milvus 向量与 kb_chunk")
    void deleteIndexRemovesVectorsAndChunks() {
        Long documentId = 1L;
        KbChunk chunk = new KbChunk();
        chunk.setMilvusPk("pk-1");
        KbDocument document = new KbDocument();
        document.setId(documentId);
        document.setChunkCount(3);

        when(chunkRepository.findByDocumentIdOrderBySeqNoAsc(documentId)).thenReturn(List.of(chunk));
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        pipeline.deleteIndex(documentId);

        verify(vectorStore).delete(List.of("pk-1"));
        verify(chunkRepository).deleteByDocumentId(documentId);
        assertThat(document.getChunkCount()).isZero();
        verify(documentRepository).save(document);
    }

    @Test
    @DisplayName("reindex 先删后异步入库")
    void reindexDeletesThenIngests() {
        Long documentId = 2L;
        when(chunkRepository.findByDocumentIdOrderBySeqNoAsc(documentId)).thenReturn(List.of());
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        pipeline.reindex(documentId);

        verify(chunkRepository).deleteByDocumentId(documentId);
        verify(self).ingestAsync(documentId);
    }
}
