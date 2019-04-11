package com.bc.service.common.redPacket.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by mrt on 2019/4/11 0011 下午 7:51
 */
@Data
public class StaticRecord {
    private int hour;
    private BigDecimal amount;
}
