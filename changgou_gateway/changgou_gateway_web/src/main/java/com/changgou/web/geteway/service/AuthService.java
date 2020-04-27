package com.changgou.web.geteway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //从cookie中获取jti的值
    public String getJtiFromCookie(ServerHttpRequest request) {

        HttpCookie httpcookie = request.getCookies().getFirst("uid");

        if (httpcookie!=null){
            String jti = httpcookie.getValue();
            return jti;
        }

        return null;
    }

    public String getJwtFromRedis(String jti) {

        String jwt = stringRedisTemplate.boundValueOps(jti).get();

        return jwt;
    }
}
