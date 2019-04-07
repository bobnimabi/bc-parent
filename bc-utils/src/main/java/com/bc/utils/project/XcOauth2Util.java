package com.bc.utils.project;

import com.alibaba.fastjson.JSON;
import com.bc.common.Exception.ExceptionCast;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mrt on 2018/5/25.
 * 在jwt令牌存储的信息，可以在这里获取
 * 因为未在令牌上存储任何信息，所以这个类暂时用不到
 */
public class XcOauth2Util {
    @Data
    public static class UserJwt{
        private Long id;
        private String username;
        private String name;
        private String headUrl;
        private Integer utype;
        private Long companyId;
    }
    public static UserJwt getUserJwtFromHeader(HttpServletRequest request){
        UserJwt jwtClaims = getJwtClaimsFromHeader(request);
        if (jwtClaims == null) {
            ExceptionCast.castFail("获取jwt令牌头信息失败");
        }
        return jwtClaims;
    }
    public static UserJwt getJwtClaimsFromHeader(HttpServletRequest request) {
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization) || authorization.indexOf("Bearer") < 0) {
            return null;
        }
        //从Bearer 后边开始取出token
        String token = authorization.substring(7);
        XcOauth2Util.UserJwt userJwt = null;
        try {
            //解析jwt
            Jwt decode = JwtHelper.decode(token);
            //得到 jwt中的用户信息
            String claims = decode.getClaims();
            //将jwt转为Map
            userJwt = JSON.parseObject(claims, XcOauth2Util.UserJwt.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userJwt;
    }

}
