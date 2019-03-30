package com.bc.service.login.special.entity;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LoginRequest extends RequestData {

    String username;
    String password;
    String verifycode;

}
