package com.bc.service.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@EnableDiscoveryClient
@EnableFeignClients
@EntityScan("")//扫描实体类
@ComponentScan(basePackages={"com.bc.service.api"})//扫描接口
@ComponentScan(basePackages={"com.bc.common"})//扫描common下的所有类
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
