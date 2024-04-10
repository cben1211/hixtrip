package com.hixtrip.sample.domain.pay.strategy;

import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.stereotype.Component;

/**
 * 支付成功的策略类
 *  @author czy
 */
@Component("Success")
public class SuccessPayCallback implements PayCallbackStrategy{
    @Override
    public String payCallback(CommandPay payStatus) {
        return "支付成功!";
    }
}
