package com.bc.service.login.dto;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LoginRequest {

   private String username;
   private String password;
   private String verifycode;
   private String dynamicCode;

}
