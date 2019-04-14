package com.bc.manager.redPacket.config;

import com.bc.manager.redPacket.interceptor.LogInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

	//使用Bean标签是为了使得LogInterceptor可以使用@Autowire
	@Bean
	public HandlerInterceptor getTokenInterceptor(){
		return new LogInterceptor();
	}
	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(getTokenInterceptor()).addPathPatterns("/**");
		super.addInterceptors(registry);
	}
}