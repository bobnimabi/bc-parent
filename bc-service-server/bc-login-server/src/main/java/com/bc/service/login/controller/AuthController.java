package com.bc.service.login.controller;

import com.alibaba.fastjson.JSON;
import com.bc.common.Exception.CustomException;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.service.login.dto.LoginParams;
import com.bc.service.login.exception.AuthCode;
import com.bc.common.pojo.AuthToken;
import com.bc.service.login.server.AuthService;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ImageCodeDefaultProperties;
import com.bc.service.login.valImage.ValidateCodeGenerator;
import com.bc.service.login.vo.JwtResult;
import com.bc.service.login.vo.LoginResult;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {
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


    @PostMapping("/userlogin")
    public LoginResult login(@RequestBody LoginParams loginParams, HttpServletRequest request) throws Exception{
        System.out.println("userLogin,sessionId:"+request.getSession().getId());
        if(loginParams == null || StringUtils.isEmpty(loginParams.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginParams == null || StringUtils.isEmpty(loginParams.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }


//        if (!authService.googleAuth(loginParams.getDynamicCode(),loginParams.getUsername()))
//            ExceptionCast.castFail("口令错误");
        String username = loginParams.getUsername();
        String password = loginParams.getPassword();

        //申请令牌
        AuthToken authToken =  authService.login(username,password,clientId,clientSecret,request.getRemoteHost());
        String access_token = authToken.getAccess_token();
        XcCookieUtil.saveCookie(access_token,cookieDomain,cookieMaxAge);
        //将username写入session
        request.getSession().setAttribute("username",username);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    //图片验证码
    @GetMapping("/validateImage")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        System.out.println("validateImage,sessionId:"+request.getSession().getId());
        ImageCode imageCode = validateCodeGenerator.createImageCode(request);
        request.getSession().setAttribute(VarParam.Login.SESSION_KEY_VALIDATE_IMAGE, imageCode);
        System.out.println(imageCode.getCode());
        ImageIO.write(imageCode.getImage(), "JPEG", response.getOutputStream());
    }

    //退出
    @PostMapping("/userlogout")
    public ResponseResult logout(HttpServletRequest httpRequest) throws Exception{
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

            Boolean delete = stringRedisTemplate.delete(VarParam.Login.LOGIN_FLAG_PRE + username);
            session.removeAttribute("username");
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @GetMapping("/userjwt")
    public JwtResult userjwt(HttpServletRequest httpRequest) throws Exception{
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
