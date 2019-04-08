package com.bc.service.login.controller;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.service.login.api.AuthControllerApi;
import com.bc.service.login.dto.*;
import com.bc.service.login.exception.AuthCode;
import com.bc.common.pojo.AuthToken;
import com.bc.service.login.server.AuthService;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ValidateCodeGenerator;
import com.bc.service.login.vo.JwtResult;
import com.bc.service.login.vo.LoginResult;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;
    @Value("${auth.cookieDomain}")
    private String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;
    @Autowired
    private AuthService authService;
    @Autowired
    private ValidateCodeGenerator validateCodeGenerator;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("test")
    @PreAuthorize("hasAuthority('query_salary')")
    public String test(){
        return "OK";
    }

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        //申请令牌
        AuthToken authToken =  authService.login(username,password,clientId,clientSecret,httpRequest.getRemoteHost());
        String access_token = authToken.getAccess_token();
        XcCookieUtil.saveCookie(access_token,cookieDomain,cookieMaxAge);
        //将username写入session
        httpRequest.getSession().setAttribute("username",username);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    //图片验证码
    @GetMapping("/validateImage")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ImageCode imageCode = validateCodeGenerator.createImageCode(request);
        request.getSession().setAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE, imageCode);
        ImageIO.write(imageCode.getImage(), "JPEG", response.getOutputStream());
    }



    //退出
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout(HttpServletRequest httpRequest) {
        //取出cookie中的用户身份令牌
        String uid =  XcCookieUtil.getTokenFormCookie(httpRequest);
        //删除redis中的token
        boolean result = XcTokenUtil.delToken(uid,stringRedisTemplate);
        //清除cookie
        XcCookieUtil.clearCookie(uid,cookieDomain);
        //清楚登录标志
        HttpSession session = httpRequest.getSession();
        Object usernameObj = session.getAttribute("username");
        if (null != usernameObj){
            String username = String.valueOf(usernameObj);

            Boolean delete = stringRedisTemplate.delete(VarParam.Login.LOGIN_FLAG + username);
            session.removeAttribute("username");
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt(HttpServletRequest httpRequest) {
        //取出cookie中的用户身份令牌
        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        if(uid == null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        //拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = XcTokenUtil.getUserToken(uid,stringRedisTemplate);
        if(userToken!=null){
            //将jwt令牌返回给用户
            String jwt_token = userToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }

}
