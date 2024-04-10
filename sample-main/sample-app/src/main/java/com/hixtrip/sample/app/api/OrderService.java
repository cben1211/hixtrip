package com.hixtrip.sample.app.api;

import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.client.order.vo.OrderVO;
import com.hixtrip.sample.client.order.vo.CommandPayVO;

/**
 * 订单的service层
 * 业务接口,entry 调用
 */
public interface OrderService {


    OrderVO creat(CommandOderCreateDTO commandOderCreateDTO);

    void payCallback(CommandPayDTO commandPayDTO) throws Exception;
}
