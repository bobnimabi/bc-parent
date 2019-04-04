package com.bc.service.login.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by mrt on 2019/4/3 0003 下午 6:59
 */
@Service
public class RegisterService {
    //用户注册的时候用于对密码加密
    @Autowired
    private PasswordEncoder passwordEncoder;

}
