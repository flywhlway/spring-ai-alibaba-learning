package com.flywhl.saa.knowledgeqa.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.knowledgeqa.model.entity.QaConversation;
import com.flywhl.saa.knowledgeqa.model.entity.QaMessage;
import com.flywhl.saa.knowledgeqa.model.vo.CitationVO;
import com.flywhl.saa.knowledgeqa.model.vo.ConversationVO;
import com.flywhl.saa.knowledgeqa.model.vo.QaAnswerVO;

/**
 * QaMessage / QaConversation ↔ VO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface MessageConverter {

    ConversationVO toVo(QaConversation entity);

    @Mapping(target = "answer", source = "content")
    @Mapping(target = "citations", source = "citations")
    @Mapping(target = "usage", expression = "java(new QaAnswerVO.TokenUsageVO(entity.getInputTokens(), entity.getOutputTokens()))")
    QaAnswerVO toAnswerVo(QaMessage entity);

    default List<CitationVO> mapCitations(List<Map<String, Object>> citations) {
        if (citations == null) {
            return List.of();
        }
        return citations.stream().map(this::mapCitation).toList();
    }

    default CitationVO mapCitation(Map<String, Object> citation) {
        if (citation == null) {
            return null;
        }
        return new CitationVO(
                toLong(citation.get("documentId")),
                toString(citation.get("documentTitle")),
                toLong(citation.get("chunkId")),
                toString(citation.get("snippet")),
                toDouble(citation.get("score")));
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private static String toString(Object value) {
        return value == null ? null : value.toString();
    }
}
