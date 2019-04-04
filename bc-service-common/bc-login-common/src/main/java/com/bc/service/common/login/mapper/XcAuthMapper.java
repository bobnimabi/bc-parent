package com.bc.service.common.login.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.bc.service.common.login.entity.XcAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-04-04
 */
public interface XcAuthMapper extends BaseMapper<XcAuth> {

    @Select("SELECT xa.auth_code " +
            "FROM xc_user xu " +
            "LEFT JOIN xc_user_role xur ON xur.user_id = xu.id " +
            "LEFT JOIN xc_role xr ON xr.id = xur.role_id " +
            "LEFT JOIN xc_role_auth xra ON xra.role_id = xr.id " +
            "LEFT JOIN xc_auth xa ON xa.id = xra.auth_id " +
            "WHERE xu.id=#{userId} AND xu.`status`<>0 AND xr.`status`<>0 AND xa.`status`<>0")
    public List<XcAuth> selectAllAuth(Long userId);
}
