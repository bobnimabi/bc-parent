package com.bc.service.redPacket.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 抽奖活动
 * </p>
 * @author admin
 * @since 2019-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsAwardActive对象", description="抽奖活动")
public class VsAwardActiveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "活动名称")
    private String activeName;

    @ApiModelProperty(value = "[0无效][1有效]")
    private Integer activeStatus;

    @ApiModelProperty(value = "活动开始时间")
    private LocalDateTime timeStart;

    @ApiModelProperty(value = "活动过期时间")
    private LocalDateTime timeEnd;

    @ApiModelProperty(value = "活动每天的开始时间")
    private String dayTimeStart;

    @ApiModelProperty(value = "活动每天的结束时间")
    private String dayTimeEnd;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
