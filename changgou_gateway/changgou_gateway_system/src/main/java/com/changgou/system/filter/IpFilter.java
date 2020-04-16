package com.changgou.system.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 通过这个过滤器
 * 获取客户端的访问ip
 */
@Component
public class IpFilter implements GlobalFilter, Ordered {

    //过滤器的具体业务逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取客户端的访问ip

        System.out.println("经过了第一个过滤器！！");
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        System.out.println("ip:"+remoteAddress.getHostName());

        //放行,继续执行下面的业务
        return chain.filter(exchange);

    }

    //定义过滤器的执行优先级,返回值越小，执行优先级越高
    @Override
    public int getOrder() {
        return 1;
    }
}
