package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {

    @Autowired
    private AddressFeign addressFeign;

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private OrderFeign orderFeign;


    @RequestMapping("/ready/order")
    public String readyOrder(Model model){

        //收件人的地址信息
        List<Address> list = addressFeign.list().getData();
        model.addAttribute("address",list);

        //购物车信息
        Map cartMap = cartFeign.list();
        List<OrderItem> orderItemList = (List<OrderItem>) cartMap.get("orderItemList");
        Integer totalMoney = (Integer) cartMap.get("totalMoney");
        Integer totalNum = (Integer) cartMap.get("totalNum");

        model.addAttribute("carts",orderItemList);
        model.addAttribute("totalMoney",totalMoney);
        model.addAttribute("totalNum",totalNum);

        //加载默认收件人信息
        for (Address address : list) {
            if ("1".equals(address.getIsDefault())){
                //默认收件人
                model.addAttribute("deAddr",address);
                break;
            }
        }


        return "order";
    }

    @PostMapping("/add")
    @ResponseBody
    public Result add(@RequestBody Order order){

        Result result = orderFeign.add(order);

        return result;

    }

}
