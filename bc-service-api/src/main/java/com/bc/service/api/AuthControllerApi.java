package com.bc.service.api;

import com.bc.common.response.ResponseResult;
import com.bc.service.login.special.entity.JwtResult;
import com.bc.service.login.special.entity.LoginRequest;
import com.bc.service.login.special.entity.LoginResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户认证",description = "用户认证接口")
public interface AuthControllerApi {
    @ApiOperation("登录")
    public LoginResult login(LoginRequest loginRequest);

    @ApiOperation("退出")
    public ResponseResult logout();

    @ApiOperation("查询用户jwt令牌")
    public JwtResult userjwt();
}
