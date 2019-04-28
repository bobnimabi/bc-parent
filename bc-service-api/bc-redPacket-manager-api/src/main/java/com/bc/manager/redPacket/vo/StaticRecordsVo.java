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
    //0-23小时，总订单
    private BigDecimal[] totalHourAmount = new BigDecimal[24];
    //0-23小时，已派送金额统计
    private BigDecimal[] dispatchHourAmount = new BigDecimal[24];
    //0-23小时，待派送金额统计
    private BigDecimal[] waitHourAmount = new BigDecimal[24];
    //今天总金额
    private BigDecimal todayAmount;
    //昨天总金额
    private BigDecimal yestAmount;
    //前天总金额
    private BigDecimal beforeAmount;
    //今天总次数
    private Integer todayNum = 0;
    //昨天总次数
    private Integer yestNum = 0;
    //前天总次数
    private Integer beforeNum = 0;
}
