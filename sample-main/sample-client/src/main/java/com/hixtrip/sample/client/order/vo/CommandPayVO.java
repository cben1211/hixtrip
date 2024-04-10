package com.hixtrip.sample.client.order.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付返回对象VO
 * @author czy
 */
@Data
@Builder
@NoArgsConstructor
public class CommandPayVO {
    /**
     * 订单id
     */
    private String orderId;

    /**
     * 支付状态
     */
    private String payStatus;

    private String code;
    private String msg;
}
