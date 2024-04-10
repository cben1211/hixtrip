package com.hixtrip.sample.domain.pay.strategy;

import com.alibaba.fastjson.JSONObject;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付回调上下文
 * @author czy
 *
 */
@Slf4j
@Component
public class PayCallbackContext {

    private final Map<String,PayCallbackStrategy> strategyMap=new ConcurrentHashMap<String,PayCallbackStrategy>();
    /**
     * 注入所有实现了PayCallbackStrategy接口的Bean
     */
    @Autowired
    public void PayCallbackStrategy(Map<String,PayCallbackStrategy> strategyMap){
        this.strategyMap.clear();
        strategyMap.forEach(this.strategyMap::put);
    }
    /**
     * 返回支付回调的类型
     *
     */
    public String getPayCallbackStrategyBean(Integer payStatus) throws Exception {
        if(payStatus==null){
            throw new Exception("支付回调处理失败,支付状态不能为空");
        }
        PayCallbackPartyEnum partyEnum=PayCallbackPartyEnum.getEnumByValue(payStatus);

        if(partyEnum==null){
            throw new Exception("支付处理失败,未匹配到相应的支付状态");
        }
        switch (partyEnum){
            case FAIL ->{
                return "Fail";
            }
            case SUCCESS -> {
                return  "Success";
            }
            case REPEAT -> {
                return "Repeat";
            }
            default -> {
                throw new Exception("支付处理失败,未匹配到相应的支付状态");
            }
        }

    }
    /**
     * 支付回调处理
     * czy
     * @return
     * @param commandPay
     */
    public String finalPayCallback(CommandPay commandPay) throws Exception {
        String result="";
        log.info("PayCallbackContext#finalPayCallback--入参commandPay", JSONObject.toJSONString(commandPay));
        result=strategyMap.get(getPayCallbackStrategyBean(Integer.parseInt(commandPay.getPayStatus()))).payCallback(commandPay);
        return result;
    }
}
