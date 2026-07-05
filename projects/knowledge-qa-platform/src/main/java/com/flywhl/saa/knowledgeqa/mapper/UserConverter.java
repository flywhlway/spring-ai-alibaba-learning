package com.flywhl.saa.knowledgeqa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flywhl.saa.knowledgeqa.model.entity.SysUser;
import com.flywhl.saa.knowledgeqa.model.vo.UserVO;

/**
 * SysUser ↔ UserVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserVO toVo(SysUser entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysUser toEntity(com.flywhl.saa.knowledgeqa.model.dto.UserCreateRequest request);
}
