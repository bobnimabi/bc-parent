package com.bc.service.adjust;

import com.bc.common.feign.interceptor.FeignClientInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients //开始feignClient
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan("")//扫描实体类
@ComponentScan(basePackages={"com.bc.service.api"})//扫描接口
@ComponentScan(basePackages={"com.bc.service.adjust"})//扫描本服务
@ComponentScan(basePackages={"com.bc.common"})//扫描common下的所有类
public class AdjustServer {
    public static void main(String[] args) {
        SpringApplication.run(AdjustServer.class,args);
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
