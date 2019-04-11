package com.bc.manager.redPacket.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.common.constant.VarParam;
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
 * @since 2019-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsAwardPlayer对象", description="活动参与者")
public class VsAwardPlayerDto extends Page implements Serializable{

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户昵称")
    private String userName;

    @ApiModelProperty(value = "玩家剩余金额，单位：分")
    private BigDecimal hasAmount;

    @ApiModelProperty(value = "参与次数")
    private Integer joinTimes;

    @ApiModelProperty(value = "备注")
    private String userRemark;

    @ApiModelProperty(value = "状态[1有效][0无效]")
    private Integer playerStatus;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime registerTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    //条件分页查询
    @ApiModelProperty(value = "排序规则 1：导入时间降序 2：流水金额降序 3：抽奖次数降序")
    private Integer orderBy;

    //清除会员条件
    @ApiModelProperty(value = "保留days天的会员，剩下的全部删除")
    private Integer days;


}
