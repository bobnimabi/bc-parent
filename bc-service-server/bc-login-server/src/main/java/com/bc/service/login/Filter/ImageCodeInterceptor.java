package com.bc.service.login.Filter;

import com.bc.common.Exception.CustomException;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ImageCodeDefaultProperties;
import com.bc.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
		String imageCodeOld = redis.opsForValue().get(VarParam.Login.IMAGE_CODE + IpUtil.getIpAddress(request));

		String imageCodeNew = request.getParameter("imageCode");
		if(imageCodeNew == null) {
			ExceptionCast.castFail("验证码不能为空");
		}

		if(StringUtils.isEmpty(imageCodeOld)) {
			ExceptionCast.castFail("验证码不存在或已过期");
		}

		if(!imageCodeNew.equalsIgnoreCase(imageCodeOld)) {
			ExceptionCast.castFail("验证码不匹配");
		}
		//校验通过移除redis中的验证码
		redis.delete(VarParam.Login.IMAGE_CODE + IpUtil.getIpAddress(request));
	}
}
