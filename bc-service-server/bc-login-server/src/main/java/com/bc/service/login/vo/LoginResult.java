package com.bc.service.login.vo;

import com.bc.common.response.ResponseResult;
import com.bc.common.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor
public class LoginResult extends ResponseResult {
    private String token;
    public LoginResult(ResultCode resultCode, String token) {
        super(resultCode);
        this.token = token;
    }

}
