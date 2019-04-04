package com.bc.service.login.dto;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LoginRequest {

    String username;
    String password;
    String verifycode;

}
