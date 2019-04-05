package com.bc.service.login.pojo;

import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 *  该类是user（security已经定义）的扩展类
 *  用于携带更多的用户数据
 */
@Data
@ToString
public class UserJwt extends User {
    private Long id;
    private String name;
    private String headUrl;
    private Integer utype;
    private Long companyId;

    //简化版（boolean默认都是true）
    public UserJwt(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    //完整版
    public UserJwt(String username, //用户输入账号
                   String password,//数据库获取的密码
                   boolean enabled,//账号是否可用
                   boolean accountNonExpired, //账号未过期？
                   boolean credentialsNonExpired,//密码未过期？
                   boolean accountNonLocked,//账号未锁定？
                   Collection<? extends GrantedAuthority> authorities) {
        super(username,password,enabled,accountNonExpired,credentialsNonExpired,accountNonLocked,authorities);
    }
}
