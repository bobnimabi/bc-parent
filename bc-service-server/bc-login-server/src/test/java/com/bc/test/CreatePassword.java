package com.bc.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by mrt on 2019/3/31 0031 下午 4:16
 */
public class CreatePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("BcWebApp123"));
    }
}
