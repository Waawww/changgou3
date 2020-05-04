package com.changgou.task;

import com.changgou.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OrderTask {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0 0 * * ?")//每隔1天执行一次
    public void autoTake(){
        System.out.println(new Date());
        //每隔一天，发送一条任意的消息到order_tack 队列上
        rabbitTemplate.convertAndSend("", RabbitMQConfig.ORDER_TACK,"-");

    }

}
