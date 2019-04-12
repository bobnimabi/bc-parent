package com.bc.service.redPacket.controller;


import com.bc.common.Exception.ExceptionCast;
import com.bc.common.response.ResponseResult;
import com.bc.service.redPacket.Dto.RedPacketDto;
import com.bc.service.redPacket.server.RedPacketServer;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/")
public class RedPacketController {
    @Autowired
    private RedPacketServer packetServer;

    @ApiOperation("抽红包")
    @PostMapping("/playRedPacket")
    public ResponseResult playRedPacket(@RequestBody RedPacketDto redPacketDto) throws Exception{
        if (null == redPacketDto || StringUtils.isEmpty(redPacketDto.getUsername())) {
            ExceptionCast.castFail("无效账号");

        }
        return packetServer.playRedPacket(redPacketDto);
    }






















    @GetMapping("/test")
    @PreAuthorize("hasAuthority('query_salar')")
    public String test(){
        return  "测试成功";
    }
}
