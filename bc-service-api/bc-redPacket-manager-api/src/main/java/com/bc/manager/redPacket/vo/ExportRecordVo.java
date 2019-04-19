package com.bc.manager.redPacket.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created by mrt on 2019/4/11 0011 下午 6:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExportRecordVo extends BaseRowModel implements Serializable {
    @ExcelProperty(value = "订单号" ,index = 0)
    private String id;

    @ExcelProperty(value = "账号" ,index = 1)
    private String userName;

    @ExcelProperty(value = "金额" ,index = 2)
    private String totalAmount;

    @ExcelProperty(value = "客户端" ,index = 3)
    private String clientType;

    @ExcelProperty(value = "派送状态" ,index = 4)
    private String payStatus;

    @ExcelProperty(value = "中奖时间" ,index = 5)
    private String timeOrder;

    @ExcelProperty(value = "派送时间" ,index = 6)
    private String timePay;
}
