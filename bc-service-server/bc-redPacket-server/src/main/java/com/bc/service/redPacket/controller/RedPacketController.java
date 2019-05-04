package com.bc.service.redPacket.controller;


import com.alibaba.fastjson.JSON;
import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.IdList;
import com.bc.manager.redPacket.dto.IdLongList;
import com.bc.manager.redPacket.dto.VsPayRecordDto;
import com.bc.manager.redPacket.dto.VsRobotDto;
import com.bc.service.common.redPacket.service.IVsNavService;
import com.bc.service.redPacket.dto.RedPacketDto;
import com.bc.service.redPacket.dto.RobotLoginDto;
import com.bc.service.redPacket.server.RedPacketServer;
import com.bc.service.redPacket.server.RobotServer;
import com.bc.utils.CheckMobile;
import com.bc.utils.IpUtil;
import com.bc.utils.MyHttpResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/")
public class RedPacketController {
    @Autowired
    private RedPacketServer packetServer;
    @Autowired
    private RobotServer robotServer;

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
        return packetServer.playRedPacket(redPacketDto);
    }

    @ApiOperation("机器人：获取图片验证码")
    @GetMapping("/getImageCode")
    public ResponseResult getImageCode(@RequestParam Integer robotNum, HttpServletResponse response) throws Exception{
        if (null == robotNum) {
            ExceptionCast.castFail("未传入机器人编号");
        }
        ServletOutputStream outputStream = response.getOutputStream();
        MyHttpResult result = robotServer.getCode(robotNum);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.getHttpEntity().writeTo(out);
        byte[] bytes = out.toByteArray();
        out.close();
       return ResponseResult.SUCCESS(Base64Utils.encodeToString(bytes));
    }
    @ApiOperation("机器人：获取图片验证码")
    @GetMapping("/getVarCode2")
    public void getVarCode2(@RequestParam Integer robotNum, HttpServletResponse response) throws Exception{
        if (null == robotNum) {
            ExceptionCast.castFail("未传入机器人编号");
        }
        ServletOutputStream outputStream = response.getOutputStream();
        MyHttpResult result = robotServer.getCode(robotNum);
        result.getHttpEntity().writeTo(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    @ApiOperation("机器人：登录")
    @PostMapping("/robotLogin")
    public ResponseResult robotLogin(@RequestBody RobotLoginDto robotLoginDto) throws Exception{
        if (null == robotLoginDto || StringUtils.isEmpty(robotLoginDto.getImageCode()) || null == robotLoginDto.getRobotNum()|| null == robotLoginDto.getVarCode()) {
            ExceptionCast.castFail("参数不全");
        }
        return  robotServer.login(
                robotLoginDto.getImageCode(),
                robotLoginDto.getRobotNum(),
                robotLoginDto.getVarCode() );
    }

    @ApiOperation("机器人：增加")
    @PostMapping("/addRobot")
    public ResponseResult addRobot(@RequestBody VsRobotDto robotDto) throws Exception{
        if (null == robotDto
            || StringUtils.isEmpty(robotDto.getRobotName())
            || null == robotDto.getRobotNum()
            || StringUtils.isEmpty(robotDto.getRobotDesc())
            || StringUtils.isEmpty(robotDto.getPlatAccount())
            || StringUtils.isEmpty(robotDto.getPlatPassword())
        ) ExceptionCast.castFail("参数不全");
        return robotServer.addRobot(robotDto);
    }

    @ApiOperation("机器人：修改")
    @PostMapping("/updateRobot")
    public ResponseResult updateRobot(@RequestBody VsRobotDto robotDto) throws Exception{
        if (null == robotDto
                || null == robotDto.getId()
                || StringUtils.isEmpty(robotDto.getRobotName())
                || StringUtils.isEmpty(robotDto.getRobotDesc())
                || StringUtils.isEmpty(robotDto.getPlatAccount())
                || StringUtils.isEmpty(robotDto.getPlatPassword())
        ) ExceptionCast.castFail("参数不全");
        robotDto.setRobotNum(null);
        return robotServer.updateRobot(robotDto);
    }

    @ApiOperation("机器人：开启或关闭")
    @PostMapping("/robotStatus")
    public ResponseResult robotStatus(@RequestBody VsRobotDto robotDto) throws Exception{
        if (null == robotDto
                || null == robotDto.getRobotStatus()
        ) ExceptionCast.castFail("参数不全");
        return robotServer.robotStatus(robotDto);
    }

    @ApiOperation("人工：补单")
    @PostMapping("/repayOrder")
    public ResponseResult repayOrder(@RequestBody IdLongList idList) throws Exception{
        if (null == idList || CollectionUtils.isEmpty(idList.getIds())) {
            ExceptionCast.castInvalid("参数不全");
        }
        return packetServer.repay(idList.getIds());
    }

    @ApiOperation("标题栏：查询所有的站点")
    @GetMapping("/queryNav")
    public ResponseResult queryNav() throws Exception{
        return packetServer.queryNav();
    }

    @ApiOperation("用户分页查询中奖纪录")
    @PostMapping("/queryMyRecord")
    public ResponseResult queryMyRecord(@RequestBody VsPayRecordDto recordDto) throws Exception{
        if (null == recordDto
                || StringUtils.isEmpty(recordDto.getUserName())
                || recordDto.getCurrent() <= 0
                || recordDto.getSize() <=0
        )ExceptionCast.castFail("参数不全，或有误");
        return packetServer.queryMyRecord(recordDto);
    }

    @ApiOperation("从新初始化布隆过滤器")
    @GetMapping("/initBloomFilter")
    public void initBloomFilter() throws Exception{
        packetServer.initBloomFilter();
    }

    //@PreAuthorize("hasAuthority('query_salar')")
    @GetMapping("/test")
    public String test(){
        return  "测试成功";
    }
}
