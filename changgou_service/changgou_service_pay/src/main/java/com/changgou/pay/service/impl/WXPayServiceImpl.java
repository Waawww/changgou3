package com.changgou.pay.service.impl;

import com.changgou.pay.service.WXPayService;
import com.github.wxpay.sdk.MyConfig;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WXPayServiceImpl implements WXPayService {

    //统一下单的接口调用
    @Override
    public Map nativePay(String orderId, Integer money) {
        try {
            //1.封装请求参数
            Map<String, String> map = new HashMap();

            //封装统一下单接口的参数
            map.put("body","畅购");
            map.put("out_trade_no",orderId);

            BigDecimal payMoney = new BigDecimal("0.01");//0.01元
            BigDecimal fen = payMoney.multiply(new BigDecimal("100"));//把 0.01 元转换成 1.00分
            fen = fen.setScale(0, BigDecimal.ROUND_UP);//将1.00分 转成 1分

            map.put("total_fee",String.valueOf(fen));//total_fee 以分为单位
            map.put("spbill_create_ip","127.0.0.1");
            map.put("notify_url","http://www.baidu.com");
            map.put("trade_type","NATIVE");

            //2.基于wxpay 完成统一下单接口的调用，并获取返回结果

            MyConfig myConfig = new MyConfig();

            WXPay wxPay = new WXPay(myConfig);


            Map<String, String> result = wxPay.unifiedOrder(map);

            return result;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
