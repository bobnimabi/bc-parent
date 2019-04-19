package com.bc.manager.redPacket.interceptor;

import com.bc.common.pojo.AuthToken;
import com.bc.utils.IpUtil;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
		String uid = XcCookieUtil.getTokenFormCookie(request);
		AuthToken userToken = XcTokenUtil.getUserToken(uid, redis);
		log.info("IP:"+ IpUtil.getIpAddress(request)+" userName:"+userToken.getUsername()+" 动作："+request.getRequestURI());
		return true;
	}
}
