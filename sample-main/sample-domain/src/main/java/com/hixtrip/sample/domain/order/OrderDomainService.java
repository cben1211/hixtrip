package com.hixtrip.sample.domain.order;

import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import com.hixtrip.sample.domain.pay.strategy.PayCallbackContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 订单领域服务
 * todo 只需要实现创建订单即可
 */
@Component
public class OrderDomainService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private com.hixtrip.sample.domain.commodity.CommodityDomainService CommodityDomainService;
    /**
     * todo 需要实现
     * 创建待付款订单
     * @return
     */
    public Boolean createOrder(CommandOderCreateDTO orderDTO) {
        Boolean isCreate=false;//是否创建成功
        String skuId=orderDTO.getSkuId();
        //通过商品领域服务获取商品价格
        BigDecimal skuPrice = CommodityDomainService.getSkuPrice(skuId);//商品单价
        Integer amount = orderDTO.getAmount();//商品数量

        //需要你在infra实现, 自行定义出入参
        Order order=new Order();
        order.setId(UUID.randomUUID().toString());
        order.setSkuId(skuId);
        order.setAmount(amount);
        order.setUserId(orderDTO.getUserId());
        order.calculateAndSetMoney(amount,skuPrice);
        int result=orderRepository.createOrder(order);
        if(result>0){
            isCreate= true;
        }
        return isCreate;
    }

    /**
     * todo 需要实现
     * 待付款订单支付成功
     * @return
     */
    public void orderPaySuccess(CommandPay commandPay) throws Exception {
        //需要你在infra实现, 自行定义出入参
        //更新订单的支付状态
        orderRepository.updatePayOrder(commandPay);
        //支付回调处理  使用策略模式
        PayCallbackContext payCallbackContext=new PayCallbackContext();
        payCallbackContext.finalPayCallback(commandPay);
    }

    /**
     * todo 需要实现
     * 待付款订单支付失败   payCallback
     * @return
     */
    public void orderPayFail(CommandPay commandPay) throws Exception {
        //需要你在infra实现, 自行定义出入参
        //更新订单的支付状态
        orderRepository.updatePayOrder(commandPay);
        //支付回调处理  使用策略模式
        PayCallbackContext payCallbackContext=new PayCallbackContext();
        payCallbackContext.finalPayCallback(commandPay);
    }

}
