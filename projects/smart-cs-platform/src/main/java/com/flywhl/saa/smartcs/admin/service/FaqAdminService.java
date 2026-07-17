package com.flywhl.saa.smartcs.admin.service;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.PageResult;
import com.flywhl.saa.smartcs.mapper.FaqConverter;
import com.flywhl.saa.smartcs.model.FaqArticleStatus;
import com.flywhl.saa.smartcs.model.dto.FaqSaveRequest;
import com.flywhl.saa.smartcs.model.entity.FaqArticle;
import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.FaqArticleVO;
import com.flywhl.saa.smartcs.rag.FaqEtlPipeline;
import com.flywhl.saa.smartcs.repository.FaqArticleRepository;
import com.flywhl.saa.smartcs.service.AuditLogService;
import com.flywhl.saa.smartcs.service.AuthService;

/**
 * FAQ 管理：分页列表/详情/新建/手动 reindex。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class FaqAdminService {

    private final FaqArticleRepository faqArticleRepository;
    private final FaqConverter faqConverter;
    private final FaqEtlPipeline faqEtlPipeline;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public FaqAdminService(
            FaqArticleRepository faqArticleRepository,
            FaqConverter faqConverter,
            FaqEtlPipeline faqEtlPipeline,
            AuthService authService,
            AuditLogService auditLogService) {
        this.faqArticleRepository = faqArticleRepository;
        this.faqConverter = faqConverter;
        this.faqEtlPipeline = faqEtlPipeline;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    public PageResult<FaqArticleVO> list(String category, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        FaqArticleStatus statusEnum = parseStatus(status);
        boolean hasCategory = category != null && !category.isBlank();

        Page<FaqArticle> result;
        if (hasCategory && statusEnum != null) {
            result = faqArticleRepository.findByCategoryAndStatus(category.trim(), statusEnum, pageable);
        } else if (hasCategory) {
            result = faqArticleRepository.findByCategory(category.trim(), pageable);
        } else if (statusEnum != null) {
            result = faqArticleRepository.findByStatus(statusEnum, pageable);
        } else {
            result = faqArticleRepository.findAll(pageable);
        }

        return PageResult.of(page, size, result.getTotalElements(),
                result.getContent().stream().map(faqConverter::toVo).toList());
    }

    public FaqArticleVO get(Long id) {
        return faqConverter.toVo(requireArticle(id));
    }

    @Transactional
    public FaqArticleVO create(FaqSaveRequest request) {
        SysUser operator = authService.requireCurrentUser();
        OffsetDateTime now = OffsetDateTime.now();
        FaqArticle article = new FaqArticle();
        article.setTitle(request.title().trim());
        article.setCategory(request.category().trim());
        article.setQuestion(request.question().trim());
        article.setAnswer(request.answer().trim());
        article.setStatus(FaqArticleStatus.PENDING);
        article.setChunkCount(0);
        article.setCreatedBy(operator.getId());
        article.setCreatedAt(now);
        article.setUpdatedAt(now);
        FaqArticle saved = faqArticleRepository.save(article);

        faqEtlPipeline.reindexArticle(saved.getId());
        FaqArticle indexed = requireArticle(saved.getId());

        auditLogService.save(
                operator.getId(),
                "CREATE_FAQ",
                "faq_article",
                String.valueOf(indexed.getId()),
                Map.of("title", indexed.getTitle(), "status", indexed.getStatus().name()));

        return faqConverter.toVo(indexed);
    }

    @Transactional
    public FaqArticleVO reindex(Long id) {
        FaqArticle article = requireArticle(id);
        SysUser operator = authService.requireCurrentUser();
        faqEtlPipeline.reindexArticle(id);
        FaqArticle refreshed = requireArticle(id);
        auditLogService.save(
                operator.getId(),
                "REINDEX_FAQ",
                "faq_article",
                String.valueOf(id),
                Map.of("title", article.getTitle(), "status", refreshed.getStatus().name()));
        return faqConverter.toVo(refreshed);
    }

    private FaqArticle requireArticle(Long id) {
        return faqArticleRepository.findById(id)
                .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "FAQ 文章不存在"));
    }

    private static FaqArticleStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return FaqArticleStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(CommonResultCode.BAD_REQUEST, "非法 FAQ status: " + status);
        }
    }
}
