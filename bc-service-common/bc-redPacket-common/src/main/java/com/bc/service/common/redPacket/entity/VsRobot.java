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
 * 
 * </p>
 *
 * @author admin
 * @since 2019-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsRobot对象", description="")
public class VsRobot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "机器人名称")
    private String robotName;

    @ApiModelProperty(value = "机器人编号")
    private Integer robotNum;

    @ApiModelProperty(value = "机器人功能描述")
    private String robotDesc;

    @ApiModelProperty(value = "平台账号")
    private String platAccount;

    @ApiModelProperty(value = "状态：0停止 1运行")
    private Integer robotStatus;

    @ApiModelProperty(value = "运行信息")
    private String robotInfo;

    @ApiModelProperty(value = "登录时间")
    private LocalDateTime loginTime;

    @ApiModelProperty(value = "掉线次数")
    private Integer loseTimes;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
