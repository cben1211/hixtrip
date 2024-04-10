package com.hixtrip.sample.domain.order.repository;

import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.pay.model.CommandPay;

/**
 *
 */
public interface OrderRepository {

    //创建订单接口
    int createOrder(Order order);

    //更新支付订单状态
    int updatePayOrderStaus(CommandPay commandPay);

}
