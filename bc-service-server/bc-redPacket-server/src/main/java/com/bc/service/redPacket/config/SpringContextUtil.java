package com.bc.service.redPacket.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

        // Spring应用上下文环境
        private static ApplicationContext applicationContext;

        /**
         * 实现ApplicationContextAware接口的回调方法。设置上下文环境
         */
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) {
                SpringContextUtil.applicationContext = applicationContext;
        }

        //获取applicationContext
        public static ApplicationContext getApplicationContext() {
                return applicationContext;
        }

        //通过name获取 Bean.
        public static Object getBean(String name){
                return getApplicationContext().getBean(name);
        }

        //通过class获取Bean.
        public static <T> T getBean(Class<T> clazz){
                return getApplicationContext().getBean(clazz);
        }

        //通过name,以及Clazz返回指定的Bean
        public static <T> T getBean(String name,Class<T> clazz){
                return getApplicationContext().getBean(name, clazz);
        }
}