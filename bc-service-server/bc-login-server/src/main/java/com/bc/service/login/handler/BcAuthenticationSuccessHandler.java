package com.bc.service.login.handler;

import com.bc.service.login.pojo.UserJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by mrt on 2019/4/3 0003 下午 7:10
 */
@Component("bcAuthenticationSuccessHandler")
@Slf4j
public class BcAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${authParams.loginReturnType}")
    private String loginReturnType;

    /**
     * 登录成功后会被调用
     * 将登录信息返回给前台
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication//请求ip，session，以及认证通过后的UserDetail（我们存储的）
        ) throws IOException, ServletException {

        if (loginReturnType.equals("JSON")){
            UserJwt userJwt = (UserJwt)authentication.getDetails();
            log.info("userId:"+userJwt.getId()+",登录成功");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(authentication));
        }else{
            super.onAuthenticationSuccess(request,response,authentication);//父类的方法默认跳转uri
        }
    }
}
