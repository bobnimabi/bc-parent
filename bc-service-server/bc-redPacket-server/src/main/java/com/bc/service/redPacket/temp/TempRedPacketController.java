package com.bc.service.redPacket.temp;


import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsPayRecordDto;
import com.bc.service.redPacket.dto.RedPacketDto;
import com.bc.utils.CheckMobile;
import com.bc.utils.IpUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping
public class TempRedPacketController {
    @Autowired
    private TempRedPacketServer tempPacketServer;
    @Autowired
    private HongBaoServer hongBaoServer;

    @ApiOperation("用户：抽红包")
    @GetMapping("/playRedPacket")
    public ResponseResult playRedPacket(@RequestParam String username, HttpServletRequest request) throws Exception{
        log.info("抽红包:IP:"+IpUtil.getIpAddress(request)+" username："+username);
        if (StringUtils.isEmpty(username)) {
            ExceptionCast.castFail("无效账号");
        }
        RedPacketDto redPacketDto = new RedPacketDto();
        redPacketDto.setUsername(username);
        redPacketDto.setClientIp(IpUtil.getIpAddress(request));
        redPacketDto.setClientType(CheckMobile.isMobile(request) == true ? VarParam.RedPacketM.CLIENT_TYPE_TWO : VarParam.RedPacketM.CLIENT_TYPE_ONE);
        return tempPacketServer.playRedPacket(redPacketDto);
    }


    @ApiOperation("用户分页查询中奖纪录")
    @PostMapping("/queryMyRecord")
    public ResponseResult queryMyRecord(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto
                || StringUtils.isEmpty(recordDto.getUserName())
                || recordDto.getCurrent() <= 0
                || recordDto.getSize() <=0
        )ExceptionCast.castFail("参数不全，或有误");
        return tempPacketServer.queryMyRecord(recordDto);
    }

    @ApiOperation("查询红包是否开始")
    @GetMapping("/queryNewRecord")
    public ResponseResult getStartTime() throws Exception {
        return ResponseResult.SUCCESS(hongBaoServer.getHongBaoYuTime());
    }
}
