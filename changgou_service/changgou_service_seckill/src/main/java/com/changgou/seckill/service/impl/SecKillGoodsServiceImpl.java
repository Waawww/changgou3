package com.changgou.seckill.service.impl;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    //根据秒杀时间段，查询秒杀商品信息
    @Override
    public List<SeckillGoods> list(String time) {

        List<SeckillGoods> values = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).values();

        return values;
    }
}
