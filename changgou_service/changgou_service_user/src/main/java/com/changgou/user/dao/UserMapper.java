package com.changgou.user.dao;

import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {

    //增加积分
    @Update("update tb_user set points=points+#{point} where username=#{username}")
    int addUserPoints(@Param("username") String username,@Param("point") Integer point);

}
