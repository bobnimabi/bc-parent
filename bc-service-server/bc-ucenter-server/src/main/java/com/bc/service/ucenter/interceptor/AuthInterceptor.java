package com.bc.service.ucenter.interceptor;

import com.alibaba.fastjson.JSON;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.service.common.login.entity.VsIp;
import com.bc.service.common.login.service.IVsIpService;
import com.bc.utils.CookieUtil;
import com.bc.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private StringRedisTemplate redis;

	@Autowired
	private IVsIpService ipService;

	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response, Object handler) throws Exception {

		//校验ip
		boolean permit = false;
		String ipAddress = IpUtil.getIpAddress(request);
		List<VsIp> ips = ipService.list();
		if (CollectionUtils.isEmpty(ips)) {
			ExceptionCast.castFail("未放行任何ip");
		}
		for (VsIp ipEntity : ips) {
			if (ipEntity.getIp().equals(ipAddress)) {
				permit = true;
			}
		}
		if (!permit) {
			ExceptionCast.castFail("ip拒绝，ip:"+ipAddress);
		}

		String requestURI = request.getRequestURI();

		//放行登录,获取jwt长令牌,图片验证码
		if (requestURI.contains("/auth/userlogin")
				|| requestURI.contains("/auth/userjwt")
				|| requestURI.contains("/auth/validateImage")

		) return true;

		//1.取cookie中的短令牌
		String tokenFromCookie = getTokenFromCookie(request);
		if(StringUtils.isEmpty(tokenFromCookie)){
			//拒绝访问
			access_denied(response);
			return false;
		}
		//2.从header中取jwt长令牌（里面加密这用户的信息）
		String jwtFromHeader = getJwtFromHeader(request);
		if(StringUtils.isEmpty(jwtFromHeader)){
			//拒绝访问
			access_denied(response);
			return false;
		}
		//3.从redis校验短令牌的过期时间(1和3共同保证jwt长令牌没有过期)
		long expire = getExpire(tokenFromCookie);
		if(expire<0){
			//拒绝访问
			access_denied(response);
			return false;
		}
		//更新短令牌过期时间

		return true;
	}

	//拒绝访问
	private void access_denied(HttpServletResponse response) throws Exception{
		//拒绝访问
		//设置响应代码
		response.setStatus(200);
		//构建响应的信息
		ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
		//转成json
		String jsonString = JSON.toJSONString(responseResult);
		PrintWriter out = response.getWriter();
		out.write(jsonString);
		//转成json，设置contentType
		response.setContentType("application/json;charset=utf-8");
		//重定向的方式
//		response.setStatus(302);
//		response.setHeader("Location", "https://www.cnblogs.com/noteless/");
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
		Long expire = redis.getExpire(key, TimeUnit.SECONDS);
		return expire;
	}
}
