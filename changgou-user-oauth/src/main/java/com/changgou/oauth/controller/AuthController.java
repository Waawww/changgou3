package com.changgou.oauth.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/oauth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;


    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }


    @PostMapping("/login")
    @ResponseBody
    public Result login(String username, String password, HttpServletResponse response){
        //校验参数
        if (StringUtils.isEmpty(username)){
            throw new RuntimeException("用户名不存在");
        }
        if (StringUtils.isEmpty(password)){
            throw new RuntimeException("密码不存在");
        }

        //申请令牌 authtoken
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);

        //将jti存入cookie
        this.saveJtiToCookie(authToken.getJti(),response);

        //返回结果

        return new Result(true, StatusCode.OK,"登录成功",authToken.getJti());
    }

    //将令牌的短标识，存入到cookie
    private void saveJtiToCookie(String jti, HttpServletResponse response) {

        CookieUtil.addCookie(response,cookieDomain,"/","uid",jti,cookieMaxAge,false);


    }

}
