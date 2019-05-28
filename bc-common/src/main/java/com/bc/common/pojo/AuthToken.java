package com.bc.common.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by mrt on 2018/5/21.
 * redis存储的用户信息（token）,所有的微服务都需要使用
 */
@Data
@ToString
@NoArgsConstructor
public class AuthToken {
    private String access_token;//访问token就是短令牌，用户身份令牌
    private String refresh_token;//刷新token
    private String jwt_token;//jwt令牌
    private Long userId;
    private String username;
    private String name;
    private Integer utype;
}
