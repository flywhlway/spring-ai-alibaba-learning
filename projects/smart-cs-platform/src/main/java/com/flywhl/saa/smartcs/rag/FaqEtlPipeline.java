package com.flywhl.saa.smartcs.rag;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.flywhl.saa.smartcs.config.ScsProperties;
import com.flywhl.saa.smartcs.model.FaqArticleStatus;
import com.flywhl.saa.smartcs.model.entity.FaqArticle;
import com.flywhl.saa.smartcs.model.entity.FaqChunk;
import com.flywhl.saa.smartcs.repository.FaqArticleRepository;
import com.flywhl.saa.smartcs.repository.FaqChunkRepository;

/**
 * FAQ 种子 ETL 流水线：{@code faq_article} 文本 → {@link TokenTextSplitter} 分块 → Embedding
 * → 双写 Milvus（{@code scs_faq}）+ Elasticsearch（{@code scs-faq}）→ {@code faq_chunk} 落库
 * （{@code milvus_pk}/{@code es_doc_id} 溯源）。
 *
 * <p>启动时由 {@link #run(ApplicationArguments)} 触发 {@link #indexAllSeedFaqs()}：仅补齐
 * 尚无 {@code faq_chunk} 记录的文章（幂等，重复启动不会重复写入向量库）；单条文章处理失败仅
 * 标记该文章 {@code status=FAILED} 并记录日志，不影响其余文章继续索引，也不阻塞应用启动。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class FaqEtlPipeline implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FaqEtlPipeline.class);

    private final FaqArticleRepository articleRepository;
    private final FaqChunkRepository chunkRepository;
    private final VectorStore milvusVectorStore;
    private final VectorStore elasticsearchVectorStore;
    private final ScsProperties properties;

    public FaqEtlPipeline(
            FaqArticleRepository articleRepository,
            FaqChunkRepository chunkRepository,
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore,
            @Qualifier("elasticsearchVectorStore") VectorStore elasticsearchVectorStore,
            ScsProperties properties) {
        this.articleRepository = articleRepository;
        this.chunkRepository = chunkRepository;
        this.milvusVectorStore = milvusVectorStore;
        this.elasticsearchVectorStore = elasticsearchVectorStore;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        indexAllSeedFaqs();
    }

    /**
     * 补齐启动时缺少向量索引的 FAQ 文章（幂等：已存在 {@code faq_chunk} 的文章跳过）。
     */
    public void indexAllSeedFaqs() {
        List<FaqArticle> candidates = articleRepository.findAll().stream()
                .filter(article -> chunkRepository.findByArticleId(article.getId()).isEmpty())
                .toList();

        if (candidates.isEmpty()) {
            log.debug("FAQ 种子向量索引已就绪，无需 ETL 补齐");
            return;
        }

        log.info("检测到 {} 条 FAQ 缺少向量索引，触发 ETL 索引", candidates.size());
        for (FaqArticle article : candidates) {
            indexArticle(article);
        }
    }

    private void indexArticle(FaqArticle article) {
        try {
            String text = "问题：" + article.getQuestion() + "\n答案：" + article.getAnswer();
            List<Document> baseChunks = splitText(text);
            if (baseChunks.isEmpty()) {
                throw new IllegalStateException("FAQ 文本分块后无有效内容");
            }

            List<FaqChunk> chunkEntities = new ArrayList<>(baseChunks.size());
            OffsetDateTime now = OffsetDateTime.now();
            int seqNo = 0;
            for (Document baseChunk : baseChunks) {
                seqNo++;
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("faqArticleId", article.getId());
                metadata.put("chunkIndex", seqNo);
                metadata.put("title", article.getTitle());
                metadata.put("category", article.getCategory());

                Document milvusDoc = new Document(baseChunk.getText(), new LinkedHashMap<>(metadata));
                milvusVectorStore.add(List.of(milvusDoc));

                Document esDoc = new Document(baseChunk.getText(), new LinkedHashMap<>(metadata));
                elasticsearchVectorStore.add(List.of(esDoc));

                FaqChunk chunk = new FaqChunk();
                chunk.setArticleId(article.getId());
                chunk.setMilvusPk(milvusDoc.getId());
                chunk.setEsDocId(esDoc.getId());
                chunk.setSeqNo(seqNo);
                chunk.setTextPreview(truncatePreview(baseChunk.getText()));
                chunk.setTokenCount(estimateTokens(baseChunk.getText()));
                chunk.setCreatedAt(now);
                chunkEntities.add(chunk);
            }
            chunkRepository.saveAll(chunkEntities);

            article.setStatus(FaqArticleStatus.INDEXED);
            article.setChunkCount(chunkEntities.size());
            article.setFailReason(null);
            article.setUpdatedAt(now);
            articleRepository.save(article);
            log.info("FAQ 文章 {} 索引完成，chunk 数={}", article.getId(), chunkEntities.size());
        } catch (Exception ex) {
            log.error("FAQ 文章 {} 索引失败", article.getId(), ex);
            article.setStatus(FaqArticleStatus.FAILED);
            article.setFailReason(truncatePreview(ex.getMessage() == null ? ex.toString() : ex.getMessage()));
            article.setUpdatedAt(OffsetDateTime.now());
            articleRepository.save(article);
        }
    }

    private List<Document> splitText(String text) {
        ScsProperties.Rag rag = properties.rag();
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(rag.chunkSize())
                .build();
        return splitter.split(List.of(new Document(text)));
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
