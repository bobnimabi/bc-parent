package com.bc.service.common.redPacket.service.impl;

import com.bc.service.common.redPacket.entity.StaticRecord;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.service.common.redPacket.mapper.VsPayRecordMapper;
import com.bc.service.common.redPacket.service.IVsPayRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 充值记录 服务实现类
 * </p>
 *
 * @author admin
 * @since 2019-04-11
 */
@Service
public class VsPayRecordServiceImpl extends ServiceImpl<VsPayRecordMapper, VsPayRecord> implements IVsPayRecordService {

}
