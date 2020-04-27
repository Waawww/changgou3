package com.changgou.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApplyTokenTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Test
    public void applyToken(){

        //构建请求地址url  http://localhost:9200/oauth/token
        ServiceInstance choose = loadBalancerClient.choose("user-auth");//通过Eureka来获取
        URI uri = choose.getUri();//  http://localhost:9200
        String url = uri+"/oauth/token";

        //封装请求参数 body, headers
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itheima");
        body.add("password","itheima");

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization",this.getHttpBasic("changgou","changgou"));

        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(body,headers);

        //当后端出现401,400. 后端不对这2个异常编码进行处理，而是直接返回给前端
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        //1.发送请求
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map map = responseEntity.getBody();
        System.out.println(map);
        /*
        *
        {access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29w
         ZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4ODA
         xOTI2MCwiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbG
         lzdCJdLCJqdGkiOiIwMWU4YjEwOS1kYjBjLTQyMmQtYmJlMy1jNzcxOGMxO
         GExZDUiLCJjbGllbnRfaWQiOiJjaGFuZ2dvdSIsInVzZXJuYW1lIjoiaXRo
         ZWltYSJ9.JCMm-gvkEV3srHx1ca0Jbp-ZykOOBiSTV8wsPIEqvHxYWqbepI
         wPGIXE3LmY0_2aptNvshvBUfPWdm5eWZdn3uLX5XzyFvS__dRi66D5MiJUu
         thoBYhdUopaXZaCcllT3Js_rgR_LTdeUPUqy6WP1RvqGyVmUHnlZEkdgOxQG
         B2aRQLaBB2-GsdFpVE9WbXgr2K5cXSAsyr-frGp8JVyTPyWDUsJzk8OF-njl
         8xlyykrSzL6XvaUkkA91hLzWYhSWZsaxcGVb4JHWN3idi6uw4IifV6oXAOEC
         k4l7y72tAaEfApdQW8D-Cd6lkt857qtruOpM6dfZrQZD5p45U6Bvg,
         token_type=bearer,
         refresh_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZ
         SI6WyJhcHAiXSwiYXRpIjoiMDFlOGIxMDktZGIwYy00MjJkLWJiZTMtYzc3M
         ThjMThhMWQ1IiwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4ODAxOT
         I2MCwiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbGlzdC
         JdLCJqdGkiOiI3Nzg1MWMxYS1mMjAyLTQ0ZmMtODEyZi03ODg4OWU1ZDMwOT
         giLCJjbGllbnRfaWQiOiJjaGFuZ2dvdSIsInVzZXJuYW1lIjoiaXRoZWltYS
         J9.pioRkbciot7A3sNu6u7kcY1LrapiVehJqnemXvz0uus_iHzAknKaoj45n74
         YGE6WVdOu4iiOHIQDyAK0JuvpU5I5gEnlFzySzo_i0mKDk_kLuGgPbyMYbjAB1
         bi572FIAnbsTRXD8335U2BESwnLU1cTGfAruvk8opHk4tSPcYazAx7j-bUFlgH
         imRiSMklbEp1zOSj6ysXk1nzPkLStSpAzWJ5nD_tAPvdh6mXOIjVRbuJcSvSOF
         Z6uykTp1IkKr_tZud-NMeO68as49ijk7IzXKHhYULSrxtIYWdqkAyatvO3_l18
         5tubQxnpkSa2LYOcGOuSsuoMrnMV1mU90Ow,
         expires_in=43199,
         scope=app,
         jti=01e8b109-db0c-422d-bbe3-c7718c18a1d5}

         * */
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String value = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(value.getBytes());
        //Basic Y2hhbmdnb3U6Y2hhbmdnb3U=
        return "Basic "+new String(encode);
    }

}
