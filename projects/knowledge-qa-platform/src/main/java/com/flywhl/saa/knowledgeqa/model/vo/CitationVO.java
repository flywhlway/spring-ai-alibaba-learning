package com.flywhl.saa.knowledgeqa.model.vo;

/**
 * 引用溯源 VO。
 *
 * @author flywhl
 * @since 1.0.0
 */
public record CitationVO(
        Long documentId,
        String documentTitle,
        Long chunkId,
        String snippet,
        Double score) {
}
