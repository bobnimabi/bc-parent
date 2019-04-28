package com.bc.manager.redPacket.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class ImportPlaysDto extends BaseRowModel  implements Serializable {
    @ExcelProperty(index = 0)
    private String userName;

    @ExcelProperty(index = 1)
    private BigDecimal hasAmount;

    @ExcelProperty(index = 2)
    private Integer joinTimes;

    @ExcelProperty(index = 3)
    private String userRemark;

    /*
        作为 excel 的模型映射，需要 setter 方法
     */

}
