package com.bc.service.ucenter;

import com.bc.common.feign.interceptor.FeignClientInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


//@EnableEurekaClient
@EnableFeignClients
@ComponentScan(basePackages={"com.bc.service.common.login"})//扫描bc-login-common下的所有类
@ComponentScan(basePackages={"com.bc.service.ucenter"})//扫描本服务下的所有类
@ComponentScan(basePackages ={"com.bc.common"}) //扫描common
@EntityScan("com.bc.service.common.login.entity")//扫描实体类
@MapperScan("com.bc.service.common.login.mapper")//扫描mapper
@SpringBootApplication
public class UcServer {
    public static void main(String[] args) {
        SpringApplication.run(UcServer.class, args);
    }

    @Bean
    @LoadBalanced//开始客户端负载均衡
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

    @Bean//通过拦截器给微服务间调用前带上令牌
    public FeignClientInterceptor getFeignClientInterceptor(){
        return new FeignClientInterceptor();
    }
}
