package com.bc.service.login.Filter;


import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.service.login.exception.AuthCode;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ImageCodeDefaultProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ValidateCodeFilter extends OncePerRequestFilter {
	

	@Autowired
	private ImageCodeDefaultProperties imageCodeDefaultProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		boolean flag = false;
		for(String url : imageCodeDefaultProperties.getUrl()) {
			if(url.equals(request.getServletPath().toString())) {
				flag = true;
			}
		}
		if(flag) {
			try {
				validate(request);
			} catch (Exception e) {
				try {
					throw e;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	private void validate(HttpServletRequest request) throws Exception {
		
		ImageCode codeInSession = (ImageCode) request.getSession().getAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE);
		
		String codeInRequest = ServletRequestUtils.getRequiredStringParameter(request, "imageCode");
		
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
