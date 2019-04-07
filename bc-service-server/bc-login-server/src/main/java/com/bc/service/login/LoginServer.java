package com.bc.service.login;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages={"com.bc.service.common.login"})//扫描bc-login-common下的所有类
@ComponentScan(basePackages={"com.bc.service.login"})//扫描本服务下的所有类
@ComponentScan(basePackages ={"com.bc.common"}) //扫描common
@EntityScan("com.bc.service.common.login.entity")//扫描实体类
@MapperScan("com.bc.service.common.login.mapper")//扫描mapper
@ServletComponentScan(basePackages = {"com.bc.service.login.listener"})//扫描监听器
@SpringBootApplication
public class LoginServer {
    public static void main(String[] args) {
        SpringApplication.run(LoginServer.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
