package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.WxPayFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wxpay")
public class PayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private WxPayFeign wxPayFeign;


    //跳转到微信支付二维码页面
    @GetMapping
    public String WxPay(String orderId, Model model){
        //根据orderId查询订单信息
        Order order = orderFeign.findById(orderId).getData();
        if (order == null ){
            //如果订单不存在，返回到错误页面
            return "fail";
        }
        //如果不是未支付订单
        if (!order.getPayStatus().equals("0")){
            return "fail";
        }

        //获取微信服务的返回值
        Result result = wxPayFeign.nativePay(orderId, order.getPayMoney());
        //如果支付二维码没有，返回到错误页面
        if (result.getData() == null){
            return "fail";
        }

        Map map = (Map) result.getData();
        map.put("payMoney",order.getPayMoney());
        map.put("orderId",orderId);

        model.addAllAttributes(map);

        return "wxpay";
    }

    //支付成功页面的跳转
    @RequestMapping("/toPaySuccess")
    public String toPaySuccess(Integer payMoney,Model model){
        model.addAttribute("payMoney",payMoney);
        return "paysuccess";
    }


}
