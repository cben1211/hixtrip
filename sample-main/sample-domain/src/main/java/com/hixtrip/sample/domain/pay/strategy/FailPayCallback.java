package com.hixtrip.sample.domain.pay.strategy;

import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 支付失败回调的策略类
 *  @author czy
 */
@Component("Fail")
public class FailPayCallback implements PayCallbackStrategy{
    @Autowired
    private InventoryDomainService inventoryDomainService;
    @Override
    public String payCallback(CommandPay commandPay) {
        //调用库存服务并获取订单进行库存回滚
        return "支付失败!";
    }
}
