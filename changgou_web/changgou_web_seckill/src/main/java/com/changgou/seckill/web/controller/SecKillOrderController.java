package com.changgou.seckill.web.controller;

import com.changgou.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wseckillorder")
public class SecKillOrderController {


    @RequestMapping("/add")
    public Result add(@RequestParam("time") String time,@RequestParam("id") Long id){

        return null;
    }


}
