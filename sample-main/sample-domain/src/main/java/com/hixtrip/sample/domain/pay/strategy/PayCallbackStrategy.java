package com.hixtrip.sample.domain.pay.strategy;

import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.stereotype.Component;

/**
 * 定义支付回调策略的抽象类
 *  @author czy
 */
@Component("PayCallbackStrategy")
public interface PayCallbackStrategy {
    //支付回调的调用方法
    public abstract String payCallback(CommandPay payStatus);
}
