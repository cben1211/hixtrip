package com.hixtrip.sample.app.convertor;

import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * DTO对像 -> 领域对象转换器
 */
@Mapper
public interface CommandPayConvertor {
    CommandPayConvertor instance= Mappers.getMapper(CommandPayConvertor.class);
    CommandPay CommandPayDTOToCommandPay(CommandPayDTO commandPayDTO);
}
