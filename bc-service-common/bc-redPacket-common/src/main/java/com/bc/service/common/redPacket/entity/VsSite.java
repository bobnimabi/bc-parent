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
 * 站点设置
 * </p>
 *
 * @author admin
 * @since 2019-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsSite对象", description="站点设置")
public class VsSite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "站点名称")
    private String siteName;

    @ApiModelProperty(value = "客服网址")
    private String customerUrl;

    private String siteUrl;

    @ApiModelProperty(value = "logo")
    private String siteLogo;

    @ApiModelProperty(value = "站点公告")
    private String siteRemark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
