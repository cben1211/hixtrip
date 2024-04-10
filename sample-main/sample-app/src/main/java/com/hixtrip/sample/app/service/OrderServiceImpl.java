package com.hixtrip.sample.app.service;

import com.hixtrip.sample.app.api.OrderService;
import com.hixtrip.sample.app.convertor.CommandPayConvertor;
import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.client.order.vo.OrderVO;
import com.hixtrip.sample.client.order.vo.CommandPayVO;
import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.pay.PayDomainService;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * app层负责处理request请求，调用领域服务
 */
@Component
public class OrderServiceImpl implements OrderService {

    @Autowired
     private OrderDomainService orderDomainService;

    @Autowired
    private PayDomainService payDomainService;
    @Autowired
    private InventoryDomainService inventoryDomainService;
    /**
     * 创建订单
     * 并对返回的类型进行转换成VO
     * @param commandOderCreateDTO
     * @return
     */
    @Override
    public OrderVO creat(CommandOderCreateDTO commandOderCreateDTO) {
        OrderVO orderVO=new OrderVO();
        Integer leftInventory=0;
        Integer amount = commandOderCreateDTO.getAmount();
        String skuId=commandOderCreateDTO.getSkuId();
        //库存操作,获取库存,扣减库存
        Integer skuIdInventory = inventoryDomainService.getInventory(skuId);
        //占用库存
        Boolean isChange=inventoryDomainService.changeInventory(skuId,Long.parseLong(skuIdInventory.toString()),
                Long.parseLong(amount.toString()),  Long.parseLong(amount.toString()));
        if(isChange){
            //创建订单
            Boolean isAdd=orderDomainService.createOrder(commandOderCreateDTO);
            if (isAdd!=true){
                orderVO.setCode("-1");
                orderVO.setMsg("订单创建失败!");
            }else{
                orderVO.setMsg("订单创建成功!");
                orderVO.setCode("1");
                return orderVO;
            }
        }
        orderVO.setCode("-1");
        orderVO.setMsg("商品skuId库存不足,订单创建失败,库存仅剩:"+skuIdInventory);
        return orderVO;
    }

    /**
     * 订单支付结果回调
     * @param commandPayDTO
     */
    @Override
    public void payCallback(CommandPayDTO commandPayDTO) throws Exception {

        String payStatus=commandPayDTO.getPayStatus();
        CommandPay commandPay= CommandPayConvertor.instance.CommandPayDTOToCommandPay(commandPayDTO);
        //订单支付回调结果处理
        if("success".equals(payStatus)){//支付成功
            orderDomainService.orderPaySuccess(commandPay);
        }else if("fail".equals(payStatus)){ //支付失败
            orderDomainService.orderPayFail(commandPay);
        }

        //记录支付回调结果
        payDomainService.payRecord(commandPay);
    }
}
