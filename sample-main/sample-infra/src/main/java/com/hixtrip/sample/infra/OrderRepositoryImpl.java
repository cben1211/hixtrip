package com.hixtrip.sample.infra;

import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import com.hixtrip.sample.infra.db.convertor.OrderDOConvertor;
import com.hixtrip.sample.infra.db.dataobject.OrderDO;
import com.hixtrip.sample.infra.db.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 创建订单
     * @param order1
     * @return
     */
    @Override
    public int createOrder(Order order1) {
        //转化对象
        OrderDO orderDO1=OrderDOConvertor.INSTANCE.orderToOderDo(order1);
        //可调用mybatis进行创建订单
        int result = orderMapper.createOrder(orderDO1);
        if(result>0){//判断是否有插入数据
            //获取新创建的订单
            OrderDO orderDO=orderMapper.selectById(orderDO1.getId());
            //订单保存到redis缓存中
            redisTemplate.opsForValue().set("order:"+orderDO.getId(), orderDO);
        }
        return result;
    }

    /**
     * 更新订单付款状态
     * @param commandPay
     * @return
     */
    @Override
    public int updatePayOrder(CommandPay commandPay) {
        OrderDO orderdo=new OrderDO();
        String status=commandPay.getPayStatus();
        String orderId=commandPay.getOrderId();
        orderdo.setId(orderId);
        orderdo.setPayStatus(status);
        int count=orderMapper.updatePayOrder(orderdo);
        return count;
    }
}
