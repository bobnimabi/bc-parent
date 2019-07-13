package com.bc.service.login.interceptor;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.pojo.AuthToken;
import com.bc.service.common.login.entity.VsIp;
import com.bc.service.common.login.service.IVsIpService;
import com.bc.utils.IpUtil;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
public class LogInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private StringRedisTemplate redis;
	@Autowired
	private IVsIpService ipService;

	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response, Object handler) throws Exception {
		//校验ip
//		boolean permit = false;
//		String ipAddress = IpUtil.getIpAddress(request);
//		List<VsIp> ips = ipService.list();
//		if (CollectionUtils.isEmpty(ips)) {
//			ExceptionCast.castFail("未放行任何ip");
//		}
//		for (VsIp ipEntity : ips) {
//			if (ipEntity.getIp().equals(ipAddress)) {
//				permit = true;
//			}
//		}
//		if (!permit) {
//			ExceptionCast.castFail("ip拒绝，ip:"+ipAddress);
//		}

		//记录用户行为
		String requestURI = request.getRequestURI();
		if (requestURI.contains("/userlogin") || requestURI.contains("/validateImage")) {
			log.info("IP:"+ IpUtil.getIpAddress(request)+" 动作："+request.getRequestURI());
			return true;
		}
		String uid = XcCookieUtil.getTokenFormCookie(request);
		AuthToken userToken = XcTokenUtil.getUserToken(uid, redis);
		log.info("IP:"+ IpUtil.getIpAddress(request)+" userName:"+userToken.getUsername()+" 动作："+requestURI);
		return true;

	}
}
