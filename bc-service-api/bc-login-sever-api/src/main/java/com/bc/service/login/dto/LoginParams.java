package com.bc.service.login.dto;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LoginParams {

   private String username;
   private String password;
   private String imageCode;
   private String varCode;

}
