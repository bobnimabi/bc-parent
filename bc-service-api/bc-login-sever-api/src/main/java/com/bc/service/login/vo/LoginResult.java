package com.bc.service.login.vo;

import com.bc.common.response.ResponseResult;
import com.bc.common.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult  {
    private String token;
    private String jwt;



}
