package com.bc.manager.redPacket.dto;

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
 * 
 * </p>
 *
 * @author admin
 * @since 2019-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsNav对象", description="")
public class VsNavDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "导航名称")
    private String navName;

    @ApiModelProperty(value = "导航url")
    private String navUrl;

    @ApiModelProperty(value = "备注")
    private String navRemark;

    @ApiModelProperty(value = "1：新页面，0：原页面")
    private Integer navTarget;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
