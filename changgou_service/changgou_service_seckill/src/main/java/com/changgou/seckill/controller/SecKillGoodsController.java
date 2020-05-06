package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckillgoods")
public class SecKillGoodsController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    //根据秒杀时间段，查询秒杀商品信息
    @RequestMapping("/list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time){
        List<SeckillGoods> list = secKillGoodsService.list(time);
        return new Result<>(true, StatusCode.OK,"查询成功",list);
    }

}
