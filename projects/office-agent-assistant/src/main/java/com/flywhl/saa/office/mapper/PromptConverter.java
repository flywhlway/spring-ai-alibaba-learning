package com.flywhl.saa.office.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.flywhl.saa.office.model.entity.PromptTemplateEntity;
import com.flywhl.saa.office.model.vo.PromptTemplateVO;
@Mapper(componentModel = "spring")
public interface PromptConverter {
    @Mapping(target = "createdBy", source = "createdBy.id")
    PromptTemplateVO toVo(PromptTemplateEntity entity);
}

