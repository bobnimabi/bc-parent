package com.bc.service.ucenter.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bc.common.constant.VarParam;
import com.bc.common.pojo.AuthToken;
import com.bc.common.response.ResponseResult;
import com.bc.service.common.login.entity.XcUser;
import com.bc.service.common.login.service.IXcUserService;
import com.bc.service.login.dto.XcUserDto;
import com.bc.utils.BCryptUtil;
import com.bc.utils.project.MyBeanUtil;
import com.bc.utils.project.XcTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by mrt on 2019/4/7 0007 下午 4:01
 */
@Service
public class UcenterServer {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IXcUserService userService;

    //修改密码
    @Transactional
    public ResponseResult changePassword(String oldPass, String newPass, String uid) throws Exception {
        AuthToken authToken = XcTokenUtil.getUserToken(uid, stringRedisTemplate);
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

    public ResponseResult queryUser(AuthToken authToken) throws Exception {
        XcUser user = userService.getById(authToken.getUserId());
        user.setPassword("");
        user.setSalt("");
        if (user.getUtype() == VarParam.Login.USER_TYPE_MANAGER) {
            List<XcUser> users = userService.list(
                    new QueryWrapper<XcUser>()
                            .select(
                                    "id",
                                    "username",
                                    "name",
                                    "utype",
                                    "status",
                                    "create_time"
                            )
                            .eq("company_id", user.getCompanyId())
                            .eq("email", user.getEmail())
            );
            if (CollectionUtils.isEmpty(users)) return ResponseResult.SUCCESS(Collections.EMPTY_LIST);
            return ResponseResult.SUCCESS(users);
        }
        return ResponseResult.SUCCESS(user);
    }

    @Transactional
    public ResponseResult addChildUser(AuthToken authToken, XcUserDto userDto) throws Exception{
        XcUser user = userService.getById(authToken.getUserId());
        if (VarParam.Login.USER_TYPE_MANAGER != user.getUtype()) {
            return ResponseResult.FAIL("无权限");
        }
        XcUser user1 = new XcUser();
        MyBeanUtil.copyProperties(userDto, user1);
        user1.setPassword(BCryptUtil.encode(user1.getPassword()));
        user1.setUtype(VarParam.Login.USER_TYPE_ORDIN);
        boolean isSave = userService.save(user1);
        if (!isSave) return ResponseResult.FAIL();
        return ResponseResult.SUCCESS();
    }

    @Transactional
    public ResponseResult delChildUser(AuthToken authToken, List<Long> userIds) throws Exception{
        XcUser user = userService.getById(authToken.getUserId());
        if (VarParam.NO != user.getUtype()) {
            return ResponseResult.FAIL("无权限");
        }
        boolean isDel = userService.removeByIds(userIds);
        if (!isDel) return ResponseResult.FAIL();
        return ResponseResult.SUCCESS();
    }
}