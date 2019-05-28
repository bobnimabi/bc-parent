package com.bc.service.ucenter.controller;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.pojo.AuthToken;
import com.bc.common.response.ResponseResult;
import com.bc.service.login.dto.IdListLongDto;
import com.bc.service.login.dto.XcUserDto;
import com.bc.service.ucenter.dto.ChangePassDto;
import com.bc.service.ucenter.server.UcenterServer;
import com.bc.utils.project.XcCookieUtil;
import com.bc.utils.project.XcTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by mrt on 2019/4/7 0007 下午 3:58
 */
@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class UcenterCotroller {
    @Autowired
    private UcenterServer ucenterServer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //查询用户信息
    @PostMapping("/queryUser")
    public ResponseResult queryUser(HttpServletRequest httpRequest) throws Exception{
        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        AuthToken authToken = XcTokenUtil.getUserToken(uid, stringRedisTemplate);
        authToken.setAccess_token(null);
        authToken.setJwt_token(null);
        authToken.setRefresh_token(null);
        return ucenterServer.queryUser(authToken);
    }

    //修改密码
    @PostMapping("/changePassword")
    @PreAuthorize("hasAuthority('changePassword')")
    public ResponseResult changePassword(@RequestBody ChangePassDto passDto, HttpServletRequest httpRequest) throws Exception{
        if (null == passDto)  ExceptionCast.castFail("未收取到任何参数");
        if (StringUtils.isEmpty(passDto.getOldPass()))
            ExceptionCast.castFail("旧密码不能为空");
        if (StringUtils.isEmpty(passDto.getNewPass()))
            ExceptionCast.castFail("新密码不能为空");
        if (passDto.getNewPass().length() < 8)
            ExceptionCast.castFail("新密码的长度不能低于8位");

        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        if (StringUtils.isEmpty(uid)) return ResponseResult.FAIL("未携带身份信息，请登录");
        return ucenterServer.changePassword(passDto.getOldPass(),passDto.getNewPass(),uid);
    }


    //增加子账号
    @PostMapping("/addChildUser")
    @PreAuthorize("hasAuthority('addChildUser')")
    public ResponseResult addChildUser(@RequestBody XcUserDto userDto, HttpServletRequest httpRequest) throws Exception{
        if (null == userDto
                || StringUtils.isEmpty(userDto.getName())
                || StringUtils.isEmpty(userDto.getPassword())
                || StringUtils.isEmpty(userDto.getUsername())) {
            return ResponseResult.FAIL("参数不全");
        }
        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        AuthToken authToken = XcTokenUtil.getUserToken(uid, stringRedisTemplate);

        return ucenterServer.addChildUser(authToken,userDto);
    }


    //删除子账号
    @PostMapping("/delChildUser")
    @PreAuthorize("hasAuthority('delChildUser')")
    public ResponseResult delChildUser(@RequestBody IdListLongDto idList, HttpServletRequest httpRequest) throws Exception{
        if (null == idList || CollectionUtils.isEmpty(idList.getIds())) {
            return ResponseResult.FAIL("未传入id");
        }
        String uid = XcCookieUtil.getTokenFormCookie(httpRequest);
        AuthToken authToken = XcTokenUtil.getUserToken(uid, stringRedisTemplate);
        return ucenterServer.delChildUser(authToken,idList.getIds());
    }

    @GetMapping("test")
    public String test() throws Exception{
        return "OK";
    }
}
