package com.bc.manager.redPacket.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 活动参与者
 * </p>
 *
 * @author admin
 * @since 2019-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsAwardPlayer对象", description="活动参与者")
public class VsAwardPlayerDto extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    @ApiModelProperty(value = "玩家剩余金额，单位：分")
    private BigDecimal hasAmount;

    @ApiModelProperty(value = "可抽奖次数")
    private Integer joinTimes;

    @ApiModelProperty(value = "备注")
    private String userRemark;

    @ApiModelProperty(value = "状态[1有效][0无效]")
    private Integer playerStatus;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime registerTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "排序条件")
    private Integer orderBy;

    @ApiModelProperty(value = "清除几天前的会员")
    private Integer days;
}
