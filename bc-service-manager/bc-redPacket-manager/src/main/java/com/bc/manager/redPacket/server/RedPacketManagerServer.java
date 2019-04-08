package com.bc.manager.redPacket.server;

import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsAwardActiveDto;
import com.bc.service.common.redPacket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by mrt on 2019/4/8 0008 下午 2:00
 */
@Service
public class RedPacketManagerServer {
    @Autowired
    private IVsAwardActiveService activeService;
    @Autowired
    private IVsAwardConfigureService awardConfigureService;
    @Autowired
    private IVsAwardPlayerService playerService;
    @Autowired
    private IVsAwardPrizeService prizeService;
    @Autowired
    private IVsConfigureService configureService;
    @Autowired
    private IVsLogService logService;
    @Autowired
    private IVsMediaService mediaService;
    @Autowired
    private IVsPayRechargeService payRechargeService;
    @Autowired
    private IVsSiteService siteService;

    /**
     * 红包活动修改
     */
    public ResponseResult addActive(VsAwardActiveDto activeDto) {
        return null;
    }

}
