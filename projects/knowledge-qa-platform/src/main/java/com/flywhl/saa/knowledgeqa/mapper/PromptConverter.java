package com.flywhl.saa.knowledgeqa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.knowledgeqa.model.entity.PromptTemplateEntity;
import com.flywhl.saa.knowledgeqa.model.vo.PromptTemplateVO;

/**
 * PromptTemplateEntity ↔ PromptTemplateVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface PromptConverter {

    @Mapping(target = "createdBy", source = "createdBy.id")
    PromptTemplateVO toVo(PromptTemplateEntity entity);
}
