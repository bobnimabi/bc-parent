package com.bc.service.common.redPacket.service.impl;

import com.bc.service.common.redPacket.entity.VsLog;
import com.bc.service.common.redPacket.mapper.VsLogMapper;
import com.bc.service.common.redPacket.service.IVsLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2019-04-09
 */
@Service
public class VsLogServiceImpl extends ServiceImpl<VsLogMapper, VsLog> implements IVsLogService {

}
