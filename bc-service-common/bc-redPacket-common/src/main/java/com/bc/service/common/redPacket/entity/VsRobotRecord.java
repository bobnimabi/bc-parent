package com.bc.service.common.redPacket.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 日志记录
 * </p>
 *
 * @author admin
 * @since 2019-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsRobotRecord对象", description="日志记录")
public class VsRobotRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订单ID")
    private String payRecordId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "金额,单位：分")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "操作状态[0失败][1成功]")
    private Integer recordStatus;

    @ApiModelProperty(value = "请求信息")
    private String reqInfo;

    @ApiModelProperty(value = "响应信息")
    private String respInfo;

    @ApiModelProperty(value = "备注")
    private String recordRemark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
