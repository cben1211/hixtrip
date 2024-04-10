package com.hixtrip.sample.entry;

import com.alibaba.fastjson.JSONObject;
import com.hixtrip.sample.app.api.OrderService;
import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.client.order.vo.OrderVO;
import com.hixtrip.sample.client.order.vo.CommandPayVO;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * todo 这是你要实现的
 */
@Slf4j
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
     * todo 这是你要实现的接口
     *创建订单
     * @param commandOderCreateDTO 入参对象
     * @return 请修改出参对象 OrderVO
     */
    @PostMapping(path = "/command/order/create")
    public OrderVO order(@RequestBody CommandOderCreateDTO commandOderCreateDTO) {
        OrderVO orderVO=new OrderVO();
        orderVO.setMsg("订单创建完成!");
        orderVO.setCode("200");
        //登录信息可以在这里模拟
        //获取userid并做非空判断
        var userId = "";
        userId=commandOderCreateDTO.getUserId();
        if (StringUtils.isBlank(userId)){
            orderVO.setMsg("userId不能为空!");
            orderVO.setCode("-1");
            return orderVO;
        }
        try{
            orderVO=orderService.creat(commandOderCreateDTO);
        }catch (Exception e){
            e.printStackTrace();
            orderVO.setMsg("订单创建失败!");
            orderVO.setCode("-1");
        }
        return  orderVO;
    }

    /**
     * todo 这是模拟创建订单后，支付结果的回调通知
     * 【中、高级要求】需要使用策略模式处理至少三种场景：支付成功、支付失败、重复支付(自行设计回调报文进行重复判定)
     *     SUCCESS(1,"支付成功"),
     *     FAIL(2,"支付失败"),
     *     REPEAT(3,"重复支付");
     * @param commandPayDTO 入参对象
     * @return 请修改出参对象
     */
    @PostMapping(path = "/command/order/pay/callback")
    public Map<String,String> payCallback(@RequestBody CommandPayDTO commandPayDTO) {
        log.info("支付结果回调通知,"+ JSONObject.toJSONString(commandPayDTO));
        Map map =new HashMap();
        map.put("msg","回调处理成功!");
        map.put("code","200");
        //获取订单id,并做非空判断
        String orderId=commandPayDTO.getOrderId();
        if (StringUtils.isBlank(orderId)){
            map.put("msg","orderId不能为空!");
            map.put("code","-1");
            return map;
        }
        try{
            orderService.payCallback(commandPayDTO);
        }catch (Exception e){
            e.printStackTrace();
            map.put("msg","订单回调处理失败!");
            map.put("code","-1");
        }
        return map;
    }

}
