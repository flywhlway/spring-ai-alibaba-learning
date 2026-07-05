package com.flywhl.saa.knowledgeqa.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.stereotype.Component;

import com.flywhl.saa.knowledgeqa.model.vo.CitationVO;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;

/**
 * 引用溯源后处理：从 RAG 召回的 {@link Document} metadata 组装 {@link CitationVO}。
 *
 * <p>答案正文不内联脚注，引用独立在 citations 字段/事件中（D-15）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class CitationPostProcessor {

    private static final int SNIPPET_MAX_LEN = 240;

    private final KbChunkRepository chunkRepository;

    public CitationPostProcessor(KbChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    /**
     * 将检索结果映射为引用列表。
     */
    public List<CitationVO> toCitations(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        List<CitationVO> citations = new ArrayList<>(documents.size());
        for (Document doc : documents) {
            citations.add(toCitation(doc));
        }
        return citations;
    }

    /**
     * 使用与 RAG Advisor 相同的 {@link DocumentRetriever} 检索并组装引用（避免双检索不一致）。
     */
    public List<CitationVO> retrieve(String question, DocumentRetriever documentRetriever) {
        List<Document> documents = documentRetriever.retrieve(new Query(question));
        return toCitations(documents);
    }

    private CitationVO toCitation(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();
        Long documentId = parseLong(metadata.get("documentId"));
        Long chunkId = resolveChunkId(doc, metadata);
        String title = stringValue(metadata.get("title"));
        String snippet = truncate(doc.getText());
        return new CitationVO(documentId, title, chunkId, snippet, doc.getScore());
    }

    private Long resolveChunkId(Document doc, Map<String, Object> metadata) {
        Long fromMetadata = parseLong(metadata.get("chunkId"));
        if (fromMetadata != null) {
            return fromMetadata;
        }
        return chunkRepository.findByMilvusPk(doc.getId())
                .map(chunk -> chunk.getId())
                .orElse(null);
    }

    private static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static String truncate(String text) {
        String content = Optional.ofNullable(text).orElse("");
        if (content.length() <= SNIPPET_MAX_LEN) {
            return content;
        }
        return content.substring(0, SNIPPET_MAX_LEN) + "…";
    }
}
