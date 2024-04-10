package com.hixtrip.sample.infra;

import com.alibaba.fastjson.JSONObject;
import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import com.hixtrip.sample.infra.db.dataobject.ProductDO;
import com.hixtrip.sample.infra.exception.OutOfStockException;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * infra层是domain定义的接口具体的实现
 */
@Component
public class InventoryRepositoryImpl implements InventoryRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private Redisson redisson;

    /**
     * 获取某个sku的库存
     * 从Redis缓存中获取,已存的格式:(product:skuId,product)
     * @param skuId
     * @return
     */
    @Override
    public Integer getInventory(String skuId) {
        ProductDO productDO =(ProductDO) redisTemplate.opsForValue().get("product:" + skuId);
        Integer inventory=productDO.getAmount();
        return inventory;
    }

    /**
     * 修改库存
     * 防止超卖使用redisson实现分布式锁
     * @param skuId
     * @param sellableQuantity  可售库存
     * @param withholdingQuantity 预占库存
     * @param occupiedQuantity  占用库存
     * @return
     */
    @Override
    public Boolean changeInventory(String skuId, Long sellableQuantity, Long withholdingQuantity, Long occupiedQuantity) {
        String lockKey="product:" + skuId;
        Boolean isChange=false;

        //设置锁,防止出现并发超卖
        RLock lock=redisson.getLock(lockKey);
        lock.lock();//加锁

        //判断并修改库存
        try {
            ProductDO productDO =(ProductDO) redisTemplate.opsForValue().get("product:" + skuId);
            int amount=productDO.getAmount();
            Integer stock=null;
            if(sellableQuantity!=amount){//缓存和数据库是否一致,不一致更新缓存
                productDO.setAmount(sellableQuantity.intValue());
                redisTemplate.opsForValue().set("product:" + skuId, JSONObject.toJSON(productDO));
            }
            stock=(int) (sellableQuantity-withholdingQuantity);
            if(stock>0){//库存充足
                productDO.setAmount(stock);
                redisTemplate.opsForValue().set("product:" + skuId, JSONObject.toJSON(productDO));
                isChange=true;
            }else{ //库存不足
                throw new OutOfStockException("库存不足,可用库存为:"+sellableQuantity);
            }
        }catch (Exception e){
            e.printStackTrace();
            isChange=false;
        }finally {
            lock.unlock();//解锁
        }
        return isChange;
    }
}
