package com.bc.utils.project;

import com.alibaba.fastjson.JSON;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.pojo.AuthToken;
import com.bc.common.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created by mrt on 2019/4/7 0007 下午 3:45
 */
public class XcTokenUtil {
    /**
     * 存储到令牌到redis
     *
     * @param uid 用户身份令牌
     * @param content      内容就是AuthToken对象的内容
     * @param ttl          过期时间
     * @return
     */
    public static boolean saveToken(String uid, String content, long ttl, StringRedisTemplate stringRedisTemplate) throws Exception{
        String key = VarParam.Login.LOGIN_PRE +"user_token:" + uid;
        stringRedisTemplate.boundValueOps(key).set(content, ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }

    //删除token
    public static boolean delToken(String uid, StringRedisTemplate stringRedisTemplate) throws Exception{
        String key = VarParam.Login.LOGIN_PRE +"user_token:" + uid;
        return stringRedisTemplate.delete(key);
    }

    //从redis查询令牌
    public static AuthToken getUserToken(String uid, StringRedisTemplate stringRedisTemplate) throws Exception{
        String key = VarParam.Login.LOGIN_PRE +"user_token:" + uid;
        //从redis中取到令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(value))  ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
