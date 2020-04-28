package com.changgou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //传递令牌
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes!=null){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (request!=null){
                //获取request的头信息
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()){
                    //遍历找到 Authorization
                    String headerName = headerNames.nextElement();
                    if ("authorization".equals(headerName)){
                        String headerValue = request.getHeader(headerName);//就是authorization对应的值：Bearer jwt令牌

                        //传递令牌
                        requestTemplate.header(headerName,headerValue);
                    }
                }
            }
        }
    }
}
