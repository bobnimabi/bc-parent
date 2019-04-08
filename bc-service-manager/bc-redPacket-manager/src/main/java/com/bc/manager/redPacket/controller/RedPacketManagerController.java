package com.bc.manager.redPacket.controller;

import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsAwardActiveDto;
import com.bc.manager.redPacket.server.RedPacketManagerServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Created by mrt on 2019/4/8 0008 下午 1:45
 */
@Api("红包后台管理")
@RestController
public class RedPacketManagerController {
    @Autowired
    RedPacketManagerServer rpmServer;

    @ApiOperation("红包活动修改")
    @PostMapping("/addActive")
    public ResponseResult addActive(@RequestBody VsAwardActiveDto activeDto){
        if (null == activeDto && null == activeDto.getId()) {
            ExceptionCast.castInvalid("未传入活动的id");
        }
        if (null != activeDto.getActiveStatus()
                && (VarParam.NO != activeDto.getActiveStatus() || VarParam.YES != activeDto.getActiveStatus())) {
            ExceptionCast.castInvalid("活动状态有误");
        }
        if (null != activeDto.getTimeEnd()
                && activeDto.getTimeEnd().isBefore(LocalDateTime.now())) {
            ExceptionCast.castInvalid("活动过期过期时间不能早于当前时间");
        }
        if (null != activeDto.getActiveModel()
                && (VarParam.RedPacketM.ACTIVEMODEL_ONE != activeDto.getActiveModel()
                || VarParam.RedPacketM.ACTIVEMODEL_TWO != activeDto.getActiveModel())) {
            ExceptionCast.castInvalid("活动模式不符合规范");
        }
        return rpmServer.addActive(activeDto);
    }

}
