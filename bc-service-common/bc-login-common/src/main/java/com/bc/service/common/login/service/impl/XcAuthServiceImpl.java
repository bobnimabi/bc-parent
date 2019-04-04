package com.bc.service.common.login.service.impl;

import com.bc.service.common.login.entity.XcAuth;
import com.bc.service.common.login.mapper.XcAuthMapper;
import com.bc.service.common.login.service.IXcAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author admin
 * @since 2019-04-04
 */
@Service
public class XcAuthServiceImpl extends ServiceImpl<XcAuthMapper, XcAuth> implements IXcAuthService {

    @Autowired
    private XcAuthMapper authMapper;
    @Override
    public List<XcAuth> selectAllAuth(Long userId) {
        return authMapper.selectAllAuth(userId);
    }
}
