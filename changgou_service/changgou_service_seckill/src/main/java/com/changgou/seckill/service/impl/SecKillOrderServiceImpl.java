package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.ConfirmMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SecKillOrderService;
import com.changgou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ConfirmMessageSender confirmMessageSender;

    //秒杀下单
    @Override
    public boolean add(Long id, String time, String username) {

        //1.获取redis中的商品信息和库存信息，进行判断商品是否存在，库存是否够扣减
        //2.执行redis的预扣减库存操作，并获取扣减之后的库存值
        //3.如果扣减之后的库存值<=0，则删除redis中相应的商品信息与库存信息
        //4.基于mq完成MySQL的数据同步，进行异步下单并扣减库存（mysql中的数据)

        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).get(id);
        String redisStock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+id);
        if (StringUtils.isEmpty(redisStock)){
            return false;
        }

        int stock = Integer.parseInt(redisStock);
        if (seckillGoods ==null || stock<=0){
            return false;
        }

        //执行redis的预扣减库存操作，并获取扣减之后的库存值。.decrement方法是对应key上的value减1 | 返回的decrement就是扣减之后的值
        //decrement方法是原子性的，同时lua脚本语言也是；在操作redis中有这2种方法保证原子性。
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if (decrement<=0){
            //扣减之后没有库存了,删除redis中的商品信息和库存信息
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).delete(id);
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        }

        //发送消息给mq，要保证消息生产者对与消息的不丢失实现
        //消息体：秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //发送消息
        confirmMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));

        return true;
    }
}
