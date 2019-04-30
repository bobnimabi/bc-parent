package com.bc.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.bc.common.constant.VarParam;
import com.bc.gateway.service.AuthService;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.utils.CookieUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** 身份校验过虑器
 * @author Administrator
 * @version 1.0
 **/
@Slf4j
@Component
public class LoginFilter extends ZuulFilter {



    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //过虑器的类型
    @Override
    public String filterType() {
        /**
         pre：请求在被路由之前执行
         routing：在路由请求时调用
         post：在routing和errror过滤器之后调用
         error：处理请求时发生错误调用
         */
        return "pre";
    }

    //过虑器序号，越小越被优先执行
    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //返回true表示要执行此过虑器
        return true;
    }

    //过虑器的内容
    //测试的需求：过虑所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务。
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = requestContext.getRequest();
        //得到response
        HttpServletResponse response = requestContext.getResponse();

        String requestURI = request.getRequestURI();

        System.out.println("请求url:"+requestURI);


        //放行登录,获取jwt长令牌,图片验证码
        if (requestURI.contains("/auth/userlogin")
            || requestURI.contains("/auth/userjwt")
            || requestURI.contains("/auth/validateImage")

        ) return null;



        //1.取cookie中的短令牌
        String tokenFromCookie = getTokenFromCookie(request);
        if(StringUtils.isEmpty(tokenFromCookie)){
            //拒绝访问
            access_denied();
            return null;
        }
//        String jwtToken = authService.getJwtToken(tokenFromCookie);
//        if(StringUtils.isEmpty(jwtToken)){
//            //拒绝访问
//            access_denied();
//            return null;
//        }
        //2.从header中取jwt长令牌（里面加密这用户的信息）
        String jwtFromHeader = getJwtFromHeader(request);
        if(StringUtils.isEmpty(jwtFromHeader)){
            //拒绝访问
            access_denied();
            return null;
        }
        //3.从redis校验短令牌的过期时间(1和3共同保证jwt长令牌没有过期)
        long expire = getExpire(tokenFromCookie);
        if(expire<0){
            //拒绝访问
            access_denied();
            return null;
        }
        //更新短令牌过期时间

        return null;
    }
    //拒绝访问
    private void access_denied(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到response
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应代码
        requestContext.setResponseStatusCode(200);
        //构建响应的信息
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转成json
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        //转成json，设置contentType
        response.setContentType("application/json;charset=utf-8");
    }



    //从头取出jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        //取到jwt令牌
        String jwt = authorization.substring(7);
        return jwt;


    }
    //从cookie取出uid
    //查询身份令牌
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    //查询令牌的有效期
    public long getExpire(String access_token){
        //key
        String key = VarParam.Login.LOGIN_PRE + "user_token:"+access_token;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
}
