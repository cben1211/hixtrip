package com.hixtrip.sample.domain.pay.strategy;

import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.stereotype.Component;

/**
 * 重复支付的回调策略类
 *  @author czy
 */
@Component("Repeat")
public class RepeatPayCallback implements PayCallbackStrategy{
    @Override
    public String payCallback(CommandPay payStatus) {
        return "重复支付!";
    }
}
