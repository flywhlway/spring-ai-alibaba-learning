package com.flywhl.saa.smartcs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.smartcs.model.entity.CsMessage;
import com.flywhl.saa.smartcs.model.vo.ChatAnswerVO;
import com.flywhl.saa.smartcs.model.vo.ChatMessageVO;

/**
 * CsMessage ↔ VO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface MessageConverter {

    ChatMessageVO toMessageVo(CsMessage entity);

    @Mapping(target = "answer", source = "content")
    @Mapping(target = "usage", expression = "java(new ChatAnswerVO.TokenUsageVO(entity.getInputTokens(), entity.getOutputTokens()))")
    ChatAnswerVO toAnswerVo(CsMessage entity);
}
