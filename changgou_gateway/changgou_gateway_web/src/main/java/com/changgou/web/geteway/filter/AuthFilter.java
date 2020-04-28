package com.changgou.web.geteway.filter;

import com.changgou.web.geteway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_URL="http://localhost:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;

    //过滤器的具体业务逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //1.判断当前的请求路径是否为登录请求
        String path = request.getURI().getPath();

        if ("/api/oauth/login".equals(path) || !UrlFilter.hasAuthorize(path)){
            //直接放行
            return chain.filter(exchange);
        }

        //2.如果不是登录请求。就从cookie中获取jti的值，如果该值不存在，拒绝本次访问
        String jti = authService.getJtiFromCookie(request);
        if (StringUtils.isEmpty(jti)){
            //如果jti为空，拒绝访问
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/

            //跳转登录页面
            return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI().getPath(),exchange);

        }

        //3.如果有jti，从redis中获取jwt。如果jwt不存在，拒绝本次访问
        String jwt = authService.getJwtFromRedis(jti);
        if (StringUtils.isEmpty(jwt)){
            //如果jwt为空，拒绝访问
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/

            //跳转登录页面
            return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI().getPath(),exchange);
        }

        //4.jwt存在，对当前的请求对象进行增强，让请求携带令牌的信息
        //对请求头进行增强    Authorization
        System.out.println("jwt:"+jwt);

        request.mutate().header("Authorization","Bearer "+jwt);

        return chain.filter(exchange);
    }

    //跳转登录页面
    private Mono<Void> toLoginPage(String loginUrl, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);//状态码：302
        response.getHeaders().set("Location",loginUrl);//头信息：Location

        return response.setComplete();
    }

    //定义过滤器的执行优先级,返回值越小，执行优先级越高
    @Override
    public int getOrder() {
        return 1;
    }
}
