package com.bc.service.redPacket.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mrt on 2019/4/13 0013 下午 12:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResultVo {
    private Boolean success;
    private String msg;
}
