package com.flywhl.saa.knowledgeqa.rag;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.model.entity.KbChunk;
import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;

/**
 * 文档 ETL 流水线：MinIO 拉取 → Tika 解析 → TokenTextSplitter 分块 → Embedding → Milvus 写入 → kb_chunk 落库。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class DocumentEtlPipeline {

    private static final Logger log = LoggerFactory.getLogger(DocumentEtlPipeline.class);

    private static final Map<String, String> DEMO_INLINE_SAMPLES = Map.of(
            "policy/travel-expense-policy-2026.pdf", """
                    员工差旅费报销制度（2026 版）
                    1. 国内出差交通费：高铁二等座、经济舱机票实报实销；出租车需附行程说明。
                    2. 住宿费：一线城市上限 600 元/晚，二线城市 450 元/晚，超标部分自理。
                    3. 餐补：按出差天数发放，每天 120 元，无需发票。
                    4. 审批流程：部门经理审批后提交财务，3 个工作日内到账。
                    5. 禁止事项：私人消费、礼品采购不得计入差旅报销。
                    """,
            "manual/gateway-manual-v3.2.docx", """
                    智能网关产品手册 V3.2
                    产品型号：IGW-3200 系列。支持 MQTT、HTTP、CoAP 多协议接入。
                    固件升级：通过 OTA 平台推送，升级前需备份配置；失败常见原因为网络中断或签名校验失败。
                    安全：默认启用 TLS 1.2，设备证书由平台统一签发。
                    故障排查：指示灯红色快闪表示网络未注册，请检查 APN 与 SIM 卡状态。
                    """,
            "tech/microservice-onboarding.md", """
                    微服务接入规范
                    1. 服务命名：{业务域}-{能力}-service，全小写连字符。
                    2. 注册发现：统一接入 Nacos，命名空间与部门对应。
                    3. 配置管理：敏感配置走密钥中心，禁止写入 Git。
                    4. 可观测性：必须暴露 /actuator/prometheus，并接入统一链路追踪。
                    5. 发布流程：金丝雀 5% → 20% → 全量，每阶段观察 15 分钟。
                    """);

    private final KbDocumentRepository documentRepository;
    private final KbChunkRepository chunkRepository;
    private final MinioClient minioClient;
    private final KqaProperties properties;
    private final VectorStore vectorStore;
    private final IngestStatusTracker statusTracker;
    private final DocumentEtlPipeline self;

    public DocumentEtlPipeline(
            KbDocumentRepository documentRepository,
            KbChunkRepository chunkRepository,
            MinioClient minioClient,
            KqaProperties properties,
            VectorStore vectorStore,
            IngestStatusTracker statusTracker,
            @Lazy DocumentEtlPipeline self) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.minioClient = minioClient;
        this.properties = properties;
        this.vectorStore = vectorStore;
        this.statusTracker = statusTracker;
        this.self = self;
    }

    @Async("etlExecutor")
    public void ingestAsync(Long documentId) {
        try {
            statusTracker.markParsing(documentId);
            KbDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

            String rawText = loadDocumentText(document);
            List<Document> chunks = splitWithOverlap(rawText, document);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("文档解析后无有效文本内容");
            }

            List<KbChunk> chunkEntities = new ArrayList<>(chunks.size());
            OffsetDateTime now = OffsetDateTime.now();
            int seqNo = 0;
            for (Document chunk : chunks) {
                seqNo++;
                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("documentId", document.getId());
                metadata.put("title", document.getTitle());
                metadata.put("seqNo", seqNo);
                Document indexed = new Document(chunk.getText(), metadata);
                vectorStore.add(List.of(indexed));

                KbChunk entity = new KbChunk();
                entity.setDocument(document);
                entity.setMilvusPk(indexed.getId());
                entity.setSeqNo(seqNo);
                entity.setTextPreview(truncatePreview(indexed.getText()));
                entity.setTokenCount(estimateTokens(indexed.getText()));
                entity.setCreatedAt(now);
                chunkEntities.add(entity);
            }
            chunkRepository.saveAll(chunkEntities);
            statusTracker.markIndexed(documentId, chunkEntities.size());
            log.info("文档 {} 索引完成，chunk 数={}", documentId, chunkEntities.size());
        } catch (Exception ex) {
            log.error("文档 {} 索引失败", documentId, ex);
            statusTracker.markFailed(documentId, ex.getMessage());
        }
    }

    @Transactional
    public void deleteIndex(Long documentId) {
        List<KbChunk> chunks = chunkRepository.findByDocumentIdOrderBySeqNoAsc(documentId);
        if (!chunks.isEmpty()) {
            List<String> milvusIds = chunks.stream().map(KbChunk::getMilvusPk).toList();
            vectorStore.delete(milvusIds);
        }
        try {
            FilterExpressionBuilder filter = new FilterExpressionBuilder();
            vectorStore.delete(filter.eq("documentId", String.valueOf(documentId)).build());
        } catch (Exception ex) {
            log.debug("按 metadata 删除 Milvus 向量时忽略: {}", ex.getMessage());
        }
        chunkRepository.deleteByDocumentId(documentId);
        KbDocument document = documentRepository.findById(documentId).orElse(null);
        if (document != null) {
            document.setChunkCount(0);
            document.setUpdatedAt(OffsetDateTime.now());
            documentRepository.save(document);
        }
    }

    public void reindex(Long documentId) {
        deleteIndex(documentId);
        self.ingestAsync(documentId);
    }

    /**
     * 使用内联样本文本直接索引（MinIO 缺演示文件时由 Seeder 调用）。
     */
    @Async("etlExecutor")
    public void ingestInlineSampleAsync(Long documentId, String sampleText) {
        try {
            statusTracker.markParsing(documentId);
            KbDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

            List<Document> chunks = splitWithOverlap(sampleText, document);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("样本文本无有效内容");
            }
            indexChunks(document, chunks);
            statusTracker.markIndexed(documentId, chunks.size());
            log.info("文档 {} 使用内联样本索引完成，chunk 数={}", documentId, chunks.size());
        } catch (Exception ex) {
            log.error("文档 {} 内联样本索引失败", documentId, ex);
            statusTracker.markFailed(documentId, ex.getMessage());
        }
    }

    private String loadDocumentText(KbDocument document) throws Exception {
        String objectKey = document.getMinioObject();
        String bucket = properties.minio().bucket();
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
            return reader.get().stream()
                    .map(Document::getText)
                    .filter(text -> text != null && !text.isBlank())
                    .reduce("", (a, b) -> a + "\n" + b)
                    .trim();
        } catch (ErrorResponseException ex) {
            log.warn("MinIO 对象 {} 不可用，尝试内联演示样本: {}", objectKey, ex.getMessage());
            return resolveInlineSample(objectKey)
                    .orElseThrow(() -> new IllegalStateException("MinIO 文件缺失且无内联样本: " + objectKey));
        }
    }

    private Optional<String> resolveInlineSample(String objectKey) {
        return Optional.ofNullable(DEMO_INLINE_SAMPLES.get(objectKey));
    }

    private List<Document> splitWithOverlap(String text, KbDocument document) {
        KqaProperties.Rag rag = properties.rag();
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(rag.chunkSize())
                .build();
        List<Document> baseChunks = splitter.split(List.of(new Document(text)));
        return applyCharOverlap(baseChunks, rag.chunkOverlap() * 4);
    }

    private List<Document> applyCharOverlap(List<Document> chunks, int overlapChars) {
        if (overlapChars <= 0 || chunks.size() <= 1) {
            return chunks;
        }
        List<Document> overlapped = new ArrayList<>(chunks.size());
        String previousTail = "";
        for (Document chunk : chunks) {
            String merged = previousTail + chunk.getText();
            overlapped.add(new Document(merged, chunk.getMetadata()));
            String text = chunk.getText();
            previousTail = text.length() > overlapChars
                    ? text.substring(text.length() - overlapChars)
                    : text;
        }
        return overlapped;
    }

    private void indexChunks(KbDocument document, List<Document> chunks) {
        List<KbChunk> chunkEntities = new ArrayList<>(chunks.size());
        OffsetDateTime now = OffsetDateTime.now();
        int seqNo = 0;
        for (Document chunk : chunks) {
            seqNo++;
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put("documentId", document.getId());
            metadata.put("title", document.getTitle());
            metadata.put("seqNo", seqNo);
            Document indexed = new Document(chunk.getText(), metadata);
            vectorStore.add(List.of(indexed));

            KbChunk entity = new KbChunk();
            entity.setDocument(document);
            entity.setMilvusPk(indexed.getId());
            entity.setSeqNo(seqNo);
            entity.setTextPreview(truncatePreview(indexed.getText()));
            entity.setTokenCount(estimateTokens(indexed.getText()));
            entity.setCreatedAt(now);
            chunkEntities.add(entity);
        }
        chunkRepository.saveAll(chunkEntities);
    }

    private static String truncatePreview(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 512 ? text : text.substring(0, 512);
    }

    private static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}
