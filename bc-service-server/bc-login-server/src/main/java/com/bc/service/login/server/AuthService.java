package com.bc.service.login.server;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.servcie.login.googleAuth.GoogleAuthenticator;
import com.bc.service.common.login.entity.XcUser;
import com.bc.service.common.login.service.IXcUserService;
import com.bc.service.login.exception.AuthCode;
import com.bc.common.pojo.AuthToken;
import com.bc.utils.project.XcTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class AuthService {
    @Value("${auth.tokenValiditySeconds}")
    private Integer tokenValiditySeconds;
    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IXcUserService userService;

    public boolean googleAuth(String dynamicCode,String username) throws Exception{
        XcUser user = userService.getOne(new QueryWrapper<XcUser>().eq("username", username));
        if (null == user) ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
        //校验opt
        return GoogleAuthenticator.check_code_pre(user.getSalt(),dynamicCode, System.currentTimeMillis());
    }

    //用户认证申请令牌，将令牌存储到redis
    public AuthToken login(String username, String password, String clientId, String clientSecret, String ip) throws Exception {

        //防止用户多次登录
//        Object ObjIp = redis.opsForValue().get(VarParam.Login.LOGIN_FLAG_PRE + username);
//        if (null != ObjIp) {
//            String loginIp = String.valueOf(ObjIp);
//            if (loginIp.equals(ip)) {
//                ExceptionCast.cast(AuthCode.AUTH_LOGIN_SAME_REPETITION);
//            } else {
//                ExceptionCast.cast(AuthCode.AUTH_LOGIN_DIFF_REPETITION);
//            }
//        }
        //请求spring security申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        XcUser user = userService.getOne(new QueryWrapper<XcUser>().eq("username", username));
        if (null == user) ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
        authToken.setUserId(user.getId());
        authToken.setUsername(user.getUsername());
        authToken.setName(user.getName());
        authToken.setUtype(user.getUtype());
        authToken.setTenantId(user.getCompanyId());
        authToken.setChannelId(Long.parseLong(user.getEmail()));
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        //存储到redis中的内容
        String jsonString = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean result = XcTokenUtil.saveToken(access_token, jsonString, tokenValiditySeconds, redis);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        //设置登录标志
        redis.opsForValue().set(VarParam.Login.LOGIN_FLAG_PRE + username, "1", tokenValiditySeconds, TimeUnit.SECONDS);
        return authToken;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        String uri = "http://localhost:" + serverPort;
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri + "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization", httpBasic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

        //申请令牌信息
        Map bodyMap = exchange.getBody();
        System.out.println(JSON.toJSONString(bodyMap));
        if (bodyMap == null ||
                bodyMap.get("access_token") == null ||
                bodyMap.get("refresh_token") == null ||
                bodyMap.get("jti") == null) {

            //解析spring security返回的错误信息
            if (bodyMap != null && bodyMap.get("error_description") != null) {
                String error_description = (String) bodyMap.get("error_description");
                if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                } else if (error_description.indexOf("坏的凭证") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return null;
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));//用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));//jwt令牌
        return authToken;
    }

    //获取httpbasic的串
    private static String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

//    public static void main(String[] args) {
//        String httpBasic = getHttpBasic("f0jB665eADSQKEtk", "K5q8rkcbjkXeyBJW");
//        System.out.println(httpBasic);
//        //ZjBqQjY2NWVBRFNRS0V0azpLNXE4cmtjYmprWGV5QkpX
//        //ZjBqQjY2NWVBRFNRS0V0azpLNXE4cmtjYmprWGV5QkpX
//
//    }

    public boolean checkUserType(String username) throws Exception{
        XcUser user = userService.getOne(new QueryWrapper<XcUser>().eq("username",username));
        if (null == user) ExceptionCast.castFail("用户不存在");
        if (user.getStatus()==VarParam.NO) ExceptionCast.castFail("该用户已停用");
        return user.getUtype()== VarParam.Login.USER_TYPE_MANAGER;
    }
}
