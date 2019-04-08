package com.bc.service.common.redPacket.entity;

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
 * 抽奖活动
 * </p>
 *
 * @author admin
 * @since 2019-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsAwardActive对象", description="抽奖活动")
public class VsAwardActive implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "活动名称")
    private String activeName;

    @ApiModelProperty(value = "[0无效][1有效]")
    private Integer activeStatus;

    @ApiModelProperty(value = "活动开始时间")
    private LocalDateTime timeStart;

    @ApiModelProperty(value = "活动过期时间")
    private LocalDateTime timeEnd;

    @ApiModelProperty(value = "模式[1每天重新计算][2活动时间累计]")
    private Integer activeModel;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
