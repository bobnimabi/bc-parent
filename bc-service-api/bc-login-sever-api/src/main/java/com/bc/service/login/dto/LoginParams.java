package com.bc.service.login.dto;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LoginParams {
   //用户名
   private String username;
   //密码
   private String password;
   //图片验证码
   private String imageCode;
   //动态密码（测试的时候不用输入）
   private String varCode;

}
