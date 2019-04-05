package com.bc.service.login.config;

import com.bc.service.login.pojo.UserJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {
    @Autowired
    UserDetailsService myUserDetailsService;

    /**
     * 用户登录（或刷新）成功后返回给用户的信息
     * @param authentication
     * @return
     */
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        LinkedHashMap response = new LinkedHashMap();
        String name = authentication.getName();
        response.put("user_name", name);

        Object principal = authentication.getPrincipal();
        UserJwt userJwt = null;
        if(principal instanceof  UserJwt){
            userJwt = (UserJwt) principal;
        }else{
            //refresh_token时候默认不去调用userdetailService获取用户信息，这里我们手动去调用，得到 UserJwt
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(name);
            userJwt = (UserJwt) userDetails;
        }
        response.put("name", userJwt.getName());
        response.put("id", userJwt.getId());
        response.put("utype",userJwt.getUtype());
        response.put("headUrl",userJwt.getHeadUrl());
        response.put("companyId",userJwt.getCompanyId());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }
}
