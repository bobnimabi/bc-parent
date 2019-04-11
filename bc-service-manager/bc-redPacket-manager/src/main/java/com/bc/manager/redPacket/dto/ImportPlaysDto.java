package com.bc.manager.redPacket.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ImportPlaysDto extends BaseRowModel {
    @ExcelProperty(index = 0)
    private String userName;

    @ExcelProperty(index = 1)
    private String hasAmount;

    @ExcelProperty(index = 2)
    private String joinTimes;

    @ExcelProperty(index = 3)
    private String userRemark;

    /*
        作为 excel 的模型映射，需要 setter 方法
     */

}
