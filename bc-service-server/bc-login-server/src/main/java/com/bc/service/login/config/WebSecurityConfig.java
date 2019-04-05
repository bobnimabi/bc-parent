package com.bc.service.login.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * spirng-Security配置
 */
@Configuration
@EnableWebSecurity
@Order(-1)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler bcAuthenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler bcAuthenticationFailureHandler;

    /**
     * 放行的url
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/userlogin","/userlogout","/userjwt");

    }

    /**
     * Spring Boot 2配置，这里看bean注入
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        AuthenticationManager manager = super.authenticationManagerBean();
        return manager;
    }
    //采用bcrypt对密码进行编码
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证网络提交方式：
     * 1.防止csrf 跨域攻击
     * 2.http basic 认证
     * 3.form表单提交
     * 4.拦截任何request请求
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //关闭csrf防护
                .httpBasic()
                .and()
                .formLogin()
//                .loginPage("/authentication/require")
//                .loginProcessingUrl("/authentication/form")//默认/login的post会被拦截作为登录，这里自定义
//                .successHandler(bcAuthenticationSuccessHandler)
//                .failureHandler(bcAuthenticationFailureHandler)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();
    }
}
