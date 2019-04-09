package com.bc.service.common.redPacket.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 充值记录
 * </p>
 *
 * @author admin
 * @since 2019-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsPayRecharge对象", description="充值记录")
public class VsPayRecharge implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "支付状态[0未支付][1已经支付]")
    private Integer payStatus;

    @ApiModelProperty(value = "总金额 单位：分")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "支付时间")
    private LocalDateTime timePay;

    @ApiModelProperty(value = "后台通知时间")
    private LocalDateTime timeNotice;

    @ApiModelProperty(value = "核销时间")
    private LocalDateTime timeVerification;

    @ApiModelProperty(value = "作废时间")
    private LocalDateTime timeDelete;

    @ApiModelProperty(value = "商户号")
    private String prizeId;

    @ApiModelProperty(value = "商户名称")
    private String prizeName;

    @ApiModelProperty(value = "奖品类型[1红包][2谢谢参与]")
    private Integer prizeType;

    @ApiModelProperty(value = "充值状态[0作废][1有效][2已派送]")
    private Integer rechargeStatus;

    @ApiModelProperty(value = "支付信息序列化")
    private String prizeSerialize;

    @ApiModelProperty(value = "充值类型[1第三方][2固定二维码][3优惠派送]")
    private Integer rechargeType;

    @ApiModelProperty(value = "客户IP地址")
    private String clientIp;

    @ApiModelProperty(value = "客户端[1pc][2mobile]")
    private Integer clientType;

    @ApiModelProperty(value = "确认支付者")
    private String operatorPaid;

    @ApiModelProperty(value = "确认派送者")
    private String operatorDispatch;

    @ApiModelProperty(value = "删除操作者")
    private String operatorDelete;

    @ApiModelProperty(value = "同步时间")
    private LocalDateTime autoTime;

    @ApiModelProperty(value = "尝试次数")
    private Integer autoTry;

    @ApiModelProperty(value = "分配")
    private Integer autoDispatch;

    @ApiModelProperty(value = "是否已经声音提示[0否][1是]")
    private Integer isNotice;

    private String rechargeRemark;

    @ApiModelProperty(value = "下单时间")
    private LocalDateTime createTime;


}
