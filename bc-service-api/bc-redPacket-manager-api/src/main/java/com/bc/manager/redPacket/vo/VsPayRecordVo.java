package com.bc.manager.redPacket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值记录
 * </p>
 *
 * @author admin
 * @since 2019-04-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsPayRecord对象", description="充值记录")
public class VsPayRecordVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "客户IP地址")
    private String clientIp;

    @ApiModelProperty(value = "客户端[1pc][2mobile]")
    private Integer clientType;

    @ApiModelProperty(value = "打钱前余额，单位：分")
    private BigDecimal preBalance;

    @ApiModelProperty(value = "总金额 单位：分")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "打钱后余额，单位：分")
    private BigDecimal aftBalance;

    @ApiModelProperty(value = "充值类型[1人工存入][2存款优惠][3负数额度归零][4取消出款][5其他]")
    private Integer rechargeType;

    @ApiModelProperty(value = "奖品类型[1红包][2其他]")
    private Integer prizeType;

    @ApiModelProperty(value = "支付状态[0作废][1有效][2已派送]")
    private Integer payStatus;

    @ApiModelProperty(value = "确认支付者")
    private String operatorPaid;

    @ApiModelProperty(value = "确认派送者")
    private String operatorDispatch;

    @ApiModelProperty(value = "删除操作者")
    private String operatorDelete;

    @ApiModelProperty(value = "下单时间")
    private LocalDateTime timeOrder;

    @ApiModelProperty(value = "支付时间")
    private LocalDateTime timePay;

    @ApiModelProperty(value = "响应时间")
    private LocalDateTime timeNotice;

    @ApiModelProperty(value = "作废时间")
    private LocalDateTime timeDelete;

    @ApiModelProperty(value = "尝试次数")
    private Integer autoTry;

    @ApiModelProperty(value = "是否已经声音提示[0否][1是]")
    private Integer isNotice;

    @ApiModelProperty(value = "支付信息序列化")
    private String payInfo;

    @ApiModelProperty(value = "响应信息序列化")
    private String resultInfo;

    @ApiModelProperty(value = "支付备注")
    private String payRemark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
