package com.flywhl.saa.office.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.model.vo.UserVO;
@Mapper(componentModel = "spring")
public interface UserConverter {
    UserVO toVo(SysUser entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysUser toEntity(com.flywhl.saa.office.model.dto.UserCreateRequest request);
}

