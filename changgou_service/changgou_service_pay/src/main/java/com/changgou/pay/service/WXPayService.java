package com.changgou.pay.service;

import java.util.Map;

public interface WXPayService {

    Map nativePay(String orderId,Integer money);

}
