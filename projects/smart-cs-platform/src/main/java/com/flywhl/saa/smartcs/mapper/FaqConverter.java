package com.flywhl.saa.smartcs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.smartcs.model.entity.FaqArticle;
import com.flywhl.saa.smartcs.model.vo.FaqArticleVO;

/**
 * FaqArticle ↔ FaqArticleVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface FaqConverter {

    @Mapping(target = "chunkCount", source = "chunkCount")
    FaqArticleVO toVo(FaqArticle entity);
}
