package com.bc.manager.redPacket.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 抽奖奖品
 * </p>
 *
 * @author admin
 * @since 2019-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsAwardPrize对象", description="抽奖奖品")
public class VsAwardPrizeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "奖品名称")
    private String prizeName;

    @ApiModelProperty(value = "奖品数量")
    private Integer prizeStoreNums;

    @ApiModelProperty(value = "派送数量")
    private Integer prizeDrawNums;

    @ApiModelProperty(value = "奖品状态[0下架][1上架]")
    private Integer prizeStatus;

    @ApiModelProperty(value = "奖品类型[1红包][2谢谢参与]")
    private Integer prizeType;

    @ApiModelProperty(value = "中奖概率")
    private Integer prizePercent;

    @ApiModelProperty(value = "奖品排序")
    private Integer prizeOrder;

    @ApiModelProperty(value = "金额 单位：分")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "奖品备注")
    private String prizeRemark;

    @ApiModelProperty(value = "添加时间")
    private LocalDateTime createTime;


}
