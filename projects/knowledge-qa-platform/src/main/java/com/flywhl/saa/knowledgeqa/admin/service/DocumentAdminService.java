package com.flywhl.saa.knowledgeqa.admin.service;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.knowledgeqa.config.KqaProperties;
import com.flywhl.saa.knowledgeqa.mapper.DocumentConverter;
import com.flywhl.saa.knowledgeqa.model.dto.DocumentUploadRequest;
import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.DocumentVO;
import com.flywhl.saa.knowledgeqa.rag.DocumentEtlPipeline;
import com.flywhl.saa.knowledgeqa.rag.IngestStatusTracker;
import com.flywhl.saa.knowledgeqa.repository.KbDocumentRepository;
import com.flywhl.saa.knowledgeqa.service.AuditLogService;
import com.flywhl.saa.knowledgeqa.service.AuthService;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

/**
 * 知识管理服务：上传落 MinIO + kb_document 登记 + 异步触发 DocumentEtlPipeline。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class DocumentAdminService {

    private final KbDocumentRepository documentRepository;
    private final DocumentConverter documentConverter;
    private final DocumentEtlPipeline documentEtlPipeline;
    private final MinioClient minioClient;
    private final KqaProperties properties;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public DocumentAdminService(
            KbDocumentRepository documentRepository,
            DocumentConverter documentConverter,
            DocumentEtlPipeline documentEtlPipeline,
            MinioClient minioClient,
            KqaProperties properties,
            AuthService authService,
            AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.documentConverter = documentConverter;
        this.documentEtlPipeline = documentEtlPipeline;
        this.minioClient = minioClient;
        this.properties = properties;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DocumentVO upload(MultipartFile file, DocumentUploadRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        SysUser uploader = authService.requireCurrentUser();
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        String objectKey = buildObjectKey(request.category(), originalName);
        String bucket = properties.minio().bucket();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build());
        } catch (Exception ex) {
            throw new BizException(CommonResultCode.INTERNAL_ERROR, "MinIO 上传失败: " + ex.getMessage());
        }

        OffsetDateTime now = OffsetDateTime.now();
        KbDocument document = new KbDocument();
        document.setTitle(request.title());
        document.setCategory(request.category());
        document.setFileName(originalName);
        document.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        document.setFileSize(file.getSize());
        document.setMinioObject(objectKey);
        document.setStatus(IngestStatusTracker.STATUS_UPLOADED);
        document.setChunkCount(0);
        document.setUploadedBy(uploader);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        KbDocument saved = documentRepository.save(document);

        documentEtlPipeline.ingestAsync(saved.getId());

        auditLogService.save(
                uploader.getId(),
                "UPLOAD_DOC",
                "kb_document",
                String.valueOf(saved.getId()),
                Map.of(
                        "title", saved.getTitle(),
                        "category", saved.getCategory(),
                        "fileName", saved.getFileName(),
                        "size", saved.getFileSize()));

        return documentConverter.toVo(saved);
    }

    public PageResult<DocumentVO> list(String status, String category, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<KbDocument> result = queryPage(status, category, pageable);
        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(documentConverter::toVo).toList());
    }

    @Transactional
    public DocumentVO reindex(Long id) {
        SysUser operator = authService.requireCurrentUser();
        KbDocument document = requireDocument(id);
        documentEtlPipeline.reindex(id);

        auditLogService.save(
                operator.getId(),
                "REINDEX_DOC",
                "kb_document",
                String.valueOf(id),
                Map.of("title", document.getTitle()));

        return documentConverter.toVo(document);
    }

    @Transactional
    public void delete(Long id) {
        SysUser operator = authService.requireCurrentUser();
        KbDocument document = requireDocument(id);

        documentEtlPipeline.deleteIndex(id);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.minio().bucket())
                    .object(document.getMinioObject())
                    .build());
        } catch (Exception ex) {
            // MinIO 对象缺失不阻塞 DB 删除
        }

        documentRepository.delete(document);

        auditLogService.save(
                operator.getId(),
                "DELETE_DOC",
                "kb_document",
                String.valueOf(id),
                Map.of("title", document.getTitle(), "fileName", document.getFileName()));
    }

    private Page<KbDocument> queryPage(String status, String category, Pageable pageable) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasCategory = category != null && !category.isBlank();
        if (hasStatus && hasCategory) {
            return documentRepository.findByStatusAndCategory(status, category, pageable);
        }
        if (hasStatus) {
            return documentRepository.findByStatus(status, pageable);
        }
        if (hasCategory) {
            return documentRepository.findByCategory(category, pageable);
        }
        return documentRepository.findAll(pageable);
    }

    private KbDocument requireDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "文档不存在"));
    }

    private static String buildObjectKey(String category, String fileName) {
        String slug = category.trim().replaceAll("\\s+", "-");
        return slug + "/" + UUID.randomUUID() + "-" + fileName;
    }
}
