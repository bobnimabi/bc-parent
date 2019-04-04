package com.bc.service.login.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bc.service.common.login.entity.XcAuth;
import com.bc.service.common.login.entity.XcUser;
import com.bc.service.common.login.service.*;
import com.bc.service.login.pojo.UserJwt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private IXcAuthService authService;
    @Autowired
    private IXcRoleAuthService roleAuthService;
    @Autowired
    private IXcRoleService roleService;
    @Autowired
    private IXcUserRoleService userRoleService;
    @Autowired
    private IXcUserService userService;



    //用户信息
    /**
     *授权码模式：申请授权码的的时候
     * 密码模式：申请令牌的时候
     * 会进入到如下方法（携带账号和密码）
     * 校验用户的账号和密码是否正确（我们编码）
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //密码
                String clientSecret = clientDetails.getClientSecret();
                return new User(username,clientSecret,AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        //使用username从数据库获取用户完整信息
        XcUser xcUser = userService.getOne(
                new QueryWrapper<XcUser>()
                        .eq("username", username)
        );
        if(xcUser == null){
            //返回空给spring security表示用户不存在
            return null;
        }
        //取出正确密码（hash值）
        String password = xcUser.getPassword();


        //从数据库获取权限
        List<XcAuth> permissions = authService.selectAllAuth(xcUser.getId());
        if(permissions == null){
            permissions = new ArrayList<XcAuth>();
        }
        List<String> user_permission = new ArrayList<>();
        permissions.forEach(item-> user_permission.add(item.getCode()));

        //使用静态的权限表示用户所拥有的权限
        String user_permission_string  = StringUtils.join(user_permission.toArray(), ",");
        UserJwt userJwt = new UserJwt(
                username,//用户输入的username
                password,//从数据库拿出来的密码
                AuthorityUtils.commaSeparatedStringToAuthorityList(user_permission_string)//从数据库拿出来的授权
        );
        userJwt.setId(userext.getId());
        userJwt.setUtype(userext.getUtype());//用户类型
        userJwt.setName(userext.getName());//用户名称
        userJwt.setUserpic(userext.getUserpic());//用户头像
        userJwt.isAccountNonExpired(true);
        userJwt.isAccountNonLocked(true);

        return userJwt;
    }
}
