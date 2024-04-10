package com.hixtrip.sample.domain.pay.strategy;

import java.util.Objects;

/**
 * 支付回调枚举
 *  @author czy
 */
public enum PayCallbackPartyEnum {
    SUCCESS(1,"支付成功"),
    FAIL(2,"支付失败"),
    REPEAT(3,"重复支付");

    private Integer value;
    private String name;
    PayCallbackPartyEnum(Integer value, String name) {
        this.value=value;
        this.name=name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 通过value获取对应的枚举项目中的name
     * @param value
     * @return
     */
    public static String getNameByValue(Integer value){
        for (PayCallbackPartyEnum sourceEnum:PayCallbackPartyEnum.values()
             ) {
            if(sourceEnum.getValue().equals(value)){
                return sourceEnum.getName();
            }
        }
        return null;
    }

    /**
     * 通过value获取支付回调的枚举项目
     * @param value
     * @return
     */
    public static PayCallbackPartyEnum getEnumByValue(Integer value){
        if(null==value){
            return null;
        }
        for (PayCallbackPartyEnum item:PayCallbackPartyEnum.values()
        ) {
            if(Objects.equals(value,item.value)){
                return item;
            }
        }
        return null;
    }
}
