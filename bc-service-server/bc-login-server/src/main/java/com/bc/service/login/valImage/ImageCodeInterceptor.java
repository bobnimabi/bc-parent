package com.bc.service.login.valImage;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.service.login.valImage.ImageCodeDefaultProperties;
import com.bc.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class ImageCodeInterceptor{
	@Autowired
	private StringRedisTemplate redis;
	@Autowired
	private ImageCodeDefaultProperties imageCodeDefaultProperties;


	public boolean match(HttpServletRequest request) throws Exception {
		boolean flag = false;
		for(String url : imageCodeDefaultProperties.getUrl()) {
			if(url.equals(request.getServletPath().toString())) {
				flag = true;
			}
		}
		return flag;
	}
	public void validate(String imageCodeNew,String imageCodeOld,HttpServletRequest request) throws Exception {

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
