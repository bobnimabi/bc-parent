package com.bc.service.common.login.service;

import com.bc.service.common.login.entity.XcAuth;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author admin
 * @since 2019-04-04
 */
public interface IXcAuthService extends IService<XcAuth> {
    List<XcAuth> selectAllAuth(Long userId);

}
