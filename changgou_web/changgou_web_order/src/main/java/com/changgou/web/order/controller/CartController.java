package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/wcart")
public class CartController {

    @Autowired
    private CartFeign cartFeign;

    //查询购物车
    @GetMapping("/list")
    public String list(Model model){
        Map map = cartFeign.list();//购物车的列表数据+商品总数+商品总价
        model.addAttribute("items",map);

        return "cart";//跳转到购物车页面

    }

    //添加购物车
    @GetMapping("/add")
    @ResponseBody
    public Result<Map> add(String skuId,Integer num){
        cartFeign.addCart(skuId, num);

        //更新完购物车后，需要重新查询购物车
        Map map = cartFeign.list();

        return new Result<>(true, StatusCode.OK,"添加购物车成功",map);
    }

}
