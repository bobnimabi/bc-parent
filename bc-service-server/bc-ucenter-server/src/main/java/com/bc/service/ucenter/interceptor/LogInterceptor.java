package com.bc.service.ucenter.interceptor;

import com.bc.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LogInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private StringRedisTemplate redis;
	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response, Object handler) throws Exception {
//		String uid = XcCookieUtil.getTokenFormCookie(request);
//		AuthToken userToken = XcTokenUtil.getUserToken(uid, redis);
//		log.info("IP:"+ IpUtil.getIpAddress(request)+" userName:"+userToken.getUsername()+" 动作："+request.getRequestURI());
		log.info("IP:"+ IpUtil.getIpAddress(request)+" 动作："+request.getRequestURI());
		return true;

	}
}
