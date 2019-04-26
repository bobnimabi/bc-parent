package com.bc.service.login.Filter;

import com.bc.common.Exception.CustomException;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ImageCodeDefaultProperties;
import com.bc.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ImageCodeInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private StringRedisTemplate redis;
	@Autowired
	private ImageCodeDefaultProperties imageCodeDefaultProperties;
	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response, Object handler) throws Exception {
		boolean flag = false;
		for(String url : imageCodeDefaultProperties.getUrl()) {
			if(url.equals(request.getServletPath().toString())) {
				flag = true;
			}
		}
		if(flag) {
			validate(request);
		}
		return true;
	}
	private void validate(HttpServletRequest request) throws Exception {

		ImageCode codeInSession = (ImageCode) request.getSession().getAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE);
		String codeInRequest = request.getParameter("imageCode");
		if(codeInRequest == null) {
			ExceptionCast.castFail("验证码不能为空");
		}

		if(codeInSession == null) {
			ExceptionCast.castFail("验证码不存在");
		}

		if(codeInSession.isExpried()) {
			request.getSession().removeAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE);
			ExceptionCast.castFail("验证码已经过期");
		}

		if(!codeInRequest.equalsIgnoreCase(codeInSession.getCode())) {
			ExceptionCast.castFail("验证码不匹配");
		}
		request.getSession().removeAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE);
	}
}
