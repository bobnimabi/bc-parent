package com.bc.service.redPacket.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by mrt on 2019/4/12 0012 下午 12:47
 */
@Data
public class RedResultVo implements Serializable {
    //0未抽中  1抽中
    private Boolean isSuccess;
    private BigDecimal Amount;
}
