package com.changgou.order.dao;

import com.changgou.order.pojo.Task;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public interface TaskMapper extends Mapper<Task> {

    //获取小于系统当前事件的数据
    @Select("select * from tb_task where update_time<#{currentTime}")
    //task实体类名和表名的映射
    @Results({
            @Result(column = "create_time",property = "createTime"),
            @Result(column = "update_time",property = "updateTime"),
            @Result(column = "delete_time",property = "deleteTime"),
            @Result(column = "task_type",property = "taskType"),
            @Result(column = "mq_exchange",property = "mqExchange"),
            @Result(column = "mq_routingkey",property = "mqRoutingkey"),
            @Result(column = "request_body",property = "requestBody"),
            @Result(column = "status",property = "status"),
            @Result(column = "errormsg",property = "errormsg")
    })
    List<Task> findTaskLessThanCurrentTime(Date currentTime);

}
