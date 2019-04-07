package com.bc.service.login.listener;

import com.bc.common.constant.VarParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

/**
 * Created by mrt on 2019/4/6.
 */
@WebListener
@Component
@Slf4j
public class SessionListener implements HttpSessionListener {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sessionCreated(HttpSessionEvent event) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        //在session销毁的时候 把loginMap中保存的键值对清除
        HttpSession session = event.getSession();
        String username = session.getAttribute("username").toString();
        if(username!=null){
            Boolean delete = stringRedisTemplate.delete(VarParam.Login.LOGIN_FLAG + username);
            session.removeAttribute("username");
            if(delete) {
                log.info(username+":用户注销成功");
                return;
            }
            log.error(username+":用户注销失败");
        }
    }
}
