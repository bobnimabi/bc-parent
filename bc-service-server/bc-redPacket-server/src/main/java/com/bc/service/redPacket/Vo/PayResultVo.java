package com.bc.service.redPacket.Vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mrt on 2019/4/13 0013 下午 12:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayResultVo {
    private Boolean success;
    private String message;
    private Integer data;
}
