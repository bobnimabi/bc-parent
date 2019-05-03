package com.bc.service.login;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by mrt on 2019/3/31 0031 下午 4:16
 */
public class CreatePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123"));
        System.out.println(encoder.matches("w90582144","$2a$10$hDw3a49r2Jzm443eG9hZYOt/w3sJABEP54A5yXYoubf8djNChur3a"));
    }
}
