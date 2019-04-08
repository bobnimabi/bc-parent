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
 * 多媒体表
 * </p>
 *
 * @author admin
 * @since 2019-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="VsMedia对象", description="多媒体表")
public class VsMedia implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "图片链接")
    private String mediaUrl;

    @ApiModelProperty(value = "内容是否有效[0无效][1有效]")
    private Integer mediaStatus;

    @ApiModelProperty(value = "多媒体类型[0图片][1视频][2声音]")
    private Integer mediaType;

    private LocalDateTime createTime;


}
