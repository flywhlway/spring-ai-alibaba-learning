package com.flywhl.saa.smartcs.mapper;

import org.mapstruct.Mapper;

import com.flywhl.saa.smartcs.model.entity.CsTicket;
import com.flywhl.saa.smartcs.model.vo.TicketVO;

/**
 * CsTicket ↔ TicketVO MapStruct 转换器。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface TicketConverter {

    TicketVO toVo(CsTicket entity);
}
