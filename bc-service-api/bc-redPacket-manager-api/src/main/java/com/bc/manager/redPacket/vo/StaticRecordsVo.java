package com.bc.manager.redPacket.vo;

import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by mrt on 2019/4/11 0011 下午 6:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StaticRecordsVo extends BaseRowModel  implements Serializable {
    //0-23小时，金额统计
    private BigDecimal[] dayHourAmount = new BigDecimal[24];
    //今天总金额
    private BigDecimal todayAmount;
    //昨天总金额
    private BigDecimal yestAmount;
    //前天总金额
    private BigDecimal beforeAmount;
}
