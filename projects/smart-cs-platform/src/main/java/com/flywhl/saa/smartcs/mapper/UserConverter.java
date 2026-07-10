package com.flywhl.saa.smartcs.mapper;

import org.mapstruct.Mapper;

import com.flywhl.saa.smartcs.model.entity.SysUser;
import com.flywhl.saa.smartcs.model.vo.UserVO;

/**
 * SysUser ↔ UserVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserVO toVo(SysUser entity);
}
