package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {

    //根据秒杀时间段，查询秒杀商品信息
    List<SeckillGoods> list(String time);

}
