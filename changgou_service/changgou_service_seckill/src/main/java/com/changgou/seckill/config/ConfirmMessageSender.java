package com.changgou.seckill.config;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ConfirmMessageSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String MESSAGE_CONFIRM_KEY="message_confirm_";

    public ConfirmMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this);
    }

    @Override
    //接收消息服务器返回的通知的，相当于一个监听方法
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if (b){
            //成功通知，删除redis中的消息内容
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM_KEY+correlationData.getId());
        }else {
            //失败的通知，把redis中的消息取出并重新发送
            Map<String,String> map = (Map<String,String>)redisTemplate.opsForHash().entries(MESSAGE_CONFIRM_KEY+correlationData.getId());
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String message = map.get("message");
            //不能再次调用rabbitTemplate发送，会导致线程死锁
            //解决办法是将map里面的信息，发送到redis中，新建一个定时轮询来重新发送
            //rabbitTemplate.convertAndSend(exchange,routingKey, JSON.toJSONString(message));
            System.out.println("============需要重新发送消息============");
        }

    }

    //


    //自定义消息的发送方法
    public void sendMessage(String exchange,String routingKey,String message){
        //设置消息的唯一标识（correlationData），并将消息存入redis
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(correlationData.getId(),message);

        //将本次发送消息的相关元数据保存到redis中
        Map<String,String> map = new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("message",message);

        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM_KEY+correlationData.getId(),map);

        //携带着本次消息的唯一标识，进行数据发送
        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);



    }


}
