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
import java.time.LocalDateTime;

/**
 * @author admin
 * @since 2019-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsRobot对象", description="")
public class VsRobotDto extends Page implements Serializable {

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

    @ApiModelProperty(value = "平台密码")
    private String platPassword;

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
