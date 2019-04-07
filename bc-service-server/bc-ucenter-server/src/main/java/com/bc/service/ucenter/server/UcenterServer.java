package com.bc.service.ucenter.server;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bc.common.pojo.AuthToken;
import com.bc.common.response.ResponseResult;
import com.bc.service.common.login.entity.XcUser;
import com.bc.service.common.login.service.IXcUserService;
import com.bc.utils.BCryptUtil;
import com.bc.utils.project.XcTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mrt on 2019/4/7 0007 下午 4:01
 */
@Service
public class UcenterServer {
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IXcUserService userService;
    //修改密码
    public ResponseResult changePassword(String oldPass, String newPass, String uid) {
        AuthToken authToken = XcTokenUtil.getUserToken(uid,stringRedisTemplate);
        XcUser user = userService.getById(authToken.getUserId());
        if (null == user) ResponseResult.FAIL("用户不存在");

        if (BCryptUtil.matches(oldPass, user.getPassword())) {
            boolean update = userService.update(
                    new UpdateWrapper<XcUser>()
                            .set("password", BCryptUtil.encode(newPass))
                            .eq("id", user.getId()));
            if (update) return ResponseResult.SUCCESS("密码修改成功");
            return ResponseResult.FAIL("密码修改失败");
        } else {
            return ResponseResult.FAIL("旧密码不正确");
        }
    }
}
