package com.bc.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.math.BigDecimal;

@EnableEurekaServer
@SpringBootApplication
public class EurkaServer {
    public static void main(String[] args) {
        SpringApplication.run(EurkaServer.class,args);
    }
}