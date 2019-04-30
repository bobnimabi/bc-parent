package com.bc.service.ucenter.dto;

import lombok.Data;

/**
 * Created by mrt on 2019/4/19 0019 下午 12:26
 */
@Data
public class ChangePassDto {
    //旧密码
    private String oldPass;
    //新密码
    private String newPass;
    //重复新密码
    private String reNewPass;
}
