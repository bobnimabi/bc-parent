package com.bc.service.common.login.entity;

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
 * @since 2019-04-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="XcUser对象", description="")
public class XcUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "账号")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String name;

    @ApiModelProperty(value = "用户类型 0超级管理员 1普通用户")
    private Integer utype;

    @ApiModelProperty(value = "用户状态：1有效 0无效")
    private Integer status;

    @ApiModelProperty(value = "性别 1男 2女")
    private Integer sex;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "盐值")
    private String salt;

    @ApiModelProperty(value = "公司id")
    private Long companyId;

    @ApiModelProperty(value = "头像")
    private String headUrl;

    @ApiModelProperty(value = "生日")
    private LocalDateTime birthday;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "qq")
    private String qq;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
