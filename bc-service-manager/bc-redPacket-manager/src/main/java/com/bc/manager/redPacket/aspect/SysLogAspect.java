package com.bc.manager.redPacket.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
 
/**
 * 系统日志切面
 */
@Slf4j
@Aspect  // 使用@Aspect注解声明一个切面
@Component
public class SysLogAspect {
    /**
     * 这里我们使用注解的形式
     * 当然，我们也可以通过切点表达式直接指定需要拦截的package,需要拦截的class 以及 method
     * 切点表达式:   execution(...)
     */
    @Pointcut("execution(public * com.bc.manager.redPacket.controller.RedPacketManagerController.*(..))")
    public void logPointCut() {}
 
    /**
     * 环绕通知 @Around  ， 当然也可以使用 @Before (前置通知)  @After (后置通知)
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            saveLog(point);
        } catch (Exception e) {
        }
        //调用目标方法
        Object result = point.proceed();
        return result;
    }
 
    /**
     * 保存日志
     */
    private void saveLog(ProceedingJoinPoint joinPoint) {
        //请求的 类名、方法名
        //请求的参数
        Object[] args = joinPoint.getArgs();
        List list = new ArrayList<String>();
        for (Object o : args) {
            list.add(o);
        }
        log.info("params:"+JSON.toJSONString(list));
    }
}
