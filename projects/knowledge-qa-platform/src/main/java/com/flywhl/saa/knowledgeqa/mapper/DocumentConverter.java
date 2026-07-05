package com.flywhl.saa.knowledgeqa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.knowledgeqa.model.entity.KbDocument;
import com.flywhl.saa.knowledgeqa.model.vo.DocumentVO;

/**
 * KbDocument ↔ DocumentVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface DocumentConverter {

    @Mapping(target = "uploadedBy", source = "uploadedBy.id")
    DocumentVO toVo(KbDocument entity);
}
