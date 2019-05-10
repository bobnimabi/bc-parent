package com.bc.service.login.controller;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.CommonCode;
import com.bc.common.response.ResponseResult;
import com.bc.service.login.valImage.ImageCodeInterceptor;
import com.bc.service.login.dto.LoginParams;
import com.bc.service.login.exception.AuthCode;
import com.bc.common.pojo.AuthToken;
import com.bc.service.login.server.AuthService;
import com.bc.service.login.valImage.ImageCode;
import com.bc.service.login.valImage.ValidateCodeGenerator;
import com.bc.service.login.vo.JwtResult;
import com.bc.service.login.vo.LoginResult;
import com.bc.utils.IpUtil;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("/")
@CrossOrigin("*")
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
    private StringRedisTemplate redis;
    @Autowired
    private ImageCodeInterceptor imageCodeInterceptor;



    @PostMapping("/userlogin")
    public ResponseResult login(@RequestBody LoginParams loginParams, HttpServletRequest request) throws Exception{

        //校验验证码
        if (loginParams == null || StringUtils.isEmpty(loginParams.getImageCode())) {
            ExceptionCast.castFail("验证码不能为空");
        }
        if(imageCodeInterceptor.match(request)) {
            String imageCodeOld = redis.opsForValue().get(VarParam.Login.IMAGE_CODE + IpUtil.getIpAddress(request));
            imageCodeInterceptor.validate(loginParams.getImageCode(),imageCodeOld,request);
        }

        //校验动态口令
        if (!authService.googleAuth(loginParams.getVarCode(),loginParams.getUsername()))
            ExceptionCast.castFail("口令错误");

        //校验用户名和密码
        if(loginParams == null || StringUtils.isEmpty(loginParams.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginParams == null || StringUtils.isEmpty(loginParams.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        String username = loginParams.getUsername();
        String password = loginParams.getPassword();

        //校验是否重复登录
        String usernameflag = redis.opsForValue().get(VarParam.Login.LOGIN_FLAG_PRE + username);
        if (StringUtils.isNotEmpty(usernameflag)) {
            ExceptionCast.castFail("不能重复登录");
        }

        //申请令牌
        AuthToken authToken =  authService.login(username,password,clientId,clientSecret,request.getRemoteHost());
        String access_token = authToken.getAccess_token();
        XcCookieUtil.saveCookie(access_token,cookieDomain,cookieMaxAge);

        LoginResult loginResult = new LoginResult(access_token,authToken.getJwt_token());
        log.info("IP:"+ IpUtil.getIpAddress(request)+" userName:"+loginParams.getUsername()+" 动作：登录成功");
        return ResponseResult.SUCCESS(loginResult);
    }

    //图片验证码
    @GetMapping("/validateImage")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ImageCode imageCode = validateCodeGenerator.createImageCode(request);
        //redis存一份
        redis.opsForValue().set(VarParam.Login.IMAGE_CODE + IpUtil.getIpAddress(request),imageCode.getCode(),imageCode.getExpireTime(), TimeUnit.SECONDS);
        ImageIO.write(imageCode.getImage(), "JPEG", response.getOutputStream());
    }

    //登出
    @PostMapping("/userlogout")
    public ResponseResult logout(HttpServletRequest httpRequest) throws Exception{
        //取出cookie中的用户身份令牌
        String uid =  XcCookieUtil.getTokenFormCookie(httpRequest);
        //删除登录标志
        AuthToken userToken = XcTokenUtil.getUserToken(uid, redis);
        redis.delete(VarParam.Login.LOGIN_FLAG_PRE + userToken.getUsername());
        //删除redis中的token
        boolean result = XcTokenUtil.delToken(uid, redis);
        //清除cookie
        XcCookieUtil.clearCookie(uid,cookieDomain);
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
        AuthToken userToken = XcTokenUtil.getUserToken(uid, redis);
        if(userToken!=null){
            //将jwt令牌返回给用户
            String jwt_token = userToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }
}
