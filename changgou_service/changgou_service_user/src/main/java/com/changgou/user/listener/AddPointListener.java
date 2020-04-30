package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AddPointListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_ADDPOINT)
    public void receiveAndPointMessage(String message){
        System.out.println("用户服务接收到了任务消息");

        //1.转换消息
        Task task = JSON.parseObject(message, Task.class);
        if (task == null && StringUtils.isEmpty(task)){
            return;
        }

        //2.判断redis中当前的任务是否存在
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if (value !=null){
            return;
        }

        //3.更新用户积分


        //向订单服务返回消息通知

    }

}
