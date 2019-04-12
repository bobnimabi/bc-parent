package com.bc.service.ucenter.controller;

import com.bc.common.pojo.AuthToken;
import com.bc.common.response.ResponseResult;
import com.bc.service.common.login.service.IXcUserService;
import com.bc.service.ucenter.server.UcenterServer;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mrt on 2019/4/7 0007 下午 3:58
 */
@RestController
@RequestMapping("/")
public class UcenterCotroller {
    @Autowired
    private UcenterServer ucenterServer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //查询用户信息
    @GetMapping("/queryUser")
    public ResponseResult queryUser(HttpServletRequest httpRequest) throws Exception{
        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        AuthToken authToken = XcTokenUtil.getUserToken(uid, stringRedisTemplate);
        authToken.setAccess_token(null);
        authToken.setJwt_token(null);
        authToken.setRefresh_token(null);
        authToken.setUserId(null);
        authToken.setUsername(null);
        return ResponseResult.SUCCESS(authToken);
    }

    //修改密码
    @PostMapping("/changePassword")
    public ResponseResult changePassword(@RequestParam String oldPass, @RequestParam String newPass, HttpServletRequest httpRequest) throws Exception{
        if (StringUtils.isEmpty(oldPass))
            return ResponseResult.INVALID_PARAM("旧密码不能为空");
        if (StringUtils.isEmpty(newPass))
            return ResponseResult.INVALID_PARAM("新密码不能为空");
        if (newPass.length() < 8)
            return ResponseResult.FAIL("新密码的长度不能低于8位");

        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        if (StringUtils.isEmpty(uid)) return ResponseResult.FAIL("未携带身份信息，请登录");
        return ucenterServer.changePassword(oldPass,newPass,uid);
    }

    @GetMapping("test")
    public String test() throws Exception{
        return "OK";
    }
}
