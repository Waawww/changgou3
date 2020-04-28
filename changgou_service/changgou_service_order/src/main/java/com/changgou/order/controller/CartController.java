package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    @GetMapping("/addCart")
    public Result addCart(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num) {
        //动态获取当前登录人的信息 username
        //String username = "itcast";
        String username = tokenDecode.getUserInfo().get("username");
        cartService.addCart(skuId,num,username);

        return new Result(true, StatusCode.OK,"加入购物车成功");

    }

    @GetMapping("/list")
    public Map list(){

        //动态获取当前登录人的信息 username
        //String username = "itcast";
        String username = tokenDecode.getUserInfo().get("username");
        Map map = cartService.list(username);

        return map;

    }

}
