package com.bc.service.redPacket.controller;


import com.bc.common.Exception.ExceptionCast;
import com.bc.common.constant.VarParam;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.VsRobotDto;
import com.bc.service.common.redPacket.entity.VsRobot;
import com.bc.service.redPacket.dto.RedPacketDto;
import com.bc.service.redPacket.dto.RobotLoginDto;
import com.bc.service.redPacket.server.RedPacketServer;
import com.bc.service.redPacket.server.RobotServer;
import com.bc.utils.CheckMobile;
import com.bc.utils.IpUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin("*")
@RestController
@RequestMapping("/")
public class RedPacketController {
    @Autowired
    private RedPacketServer packetServer;
    @Autowired
    private RobotServer robotServer;

    @ApiOperation("抽红包")
    @PostMapping("/playRedPacket")
    public ResponseResult playRedPacket(@RequestBody RedPacketDto redPacketDto, HttpServletRequest request) throws Exception{
        if (null == redPacketDto || StringUtils.isEmpty(redPacketDto.getUsername())) {
            ExceptionCast.castFail("无效账号");
        }
        redPacketDto.setClientIp(IpUtil.getIpAddress(request));
        redPacketDto.setClientType(CheckMobile.isMobile(request) == true ? VarParam.RedPacketM.CLIENT_TYPE_TWO : VarParam.RedPacketM.CLIENT_TYPE_ONE);
        return packetServer.playRedPacket(redPacketDto);
    }

    @ApiOperation("获取图片验证码")
    @PostMapping("/getVarCode")
    public void getVarCode(@RequestBody RobotLoginDto robotLoginDto, HttpServletResponse response) throws Exception{
        if (null == robotLoginDto ||  null == robotLoginDto.getRobotNum()) {
            ExceptionCast.castFail("未传入机器人编号");
        }
        ServletOutputStream outputStream = response.getOutputStream();
        robotServer.getCode(outputStream,robotLoginDto.getRobotNum());
        outputStream.flush();
        outputStream.close();
    }

    @ApiOperation("机器人登录")
    @PostMapping("/robotLogin")
    public ResponseResult robotLogin(@RequestBody RobotLoginDto robotLoginDto) throws Exception{
        if (null == robotLoginDto || StringUtils.isEmpty(robotLoginDto.getVarCode()) || null == robotLoginDto.getRobotNum()) {
            ExceptionCast.castFail("未传入验证码或机器人编号");
        }
        Integer robotNum = robotLoginDto.getRobotNum();
        return  robotServer.login(
                robotLoginDto.getVarCode(),
                robotLoginDto.getRobotNum());
    }

    @ApiOperation("机器人增加")
    @PostMapping("/addRobot")
    public ResponseResult addRobot(@RequestBody VsRobotDto robotDto) throws Exception{
        if (null == robotDto
            || StringUtils.isEmpty(robotDto.getRobotName())
            || null == robotDto.getRobotNum()
            || StringUtils.isEmpty(robotDto.getRobotDesc())
            || StringUtils.isEmpty(robotDto.getPlatAccount())
            || StringUtils.isEmpty(robotDto.getPlatPassword())
            || null == robotDto.getRobotStatus()
        ) ExceptionCast.castFail("参数不全");
        return robotServer.addRobot(robotDto);
    }

    @ApiOperation("机器人开启或关闭")
    @PostMapping("/robotStatus")
    public ResponseResult robotStatus(@RequestBody VsRobotDto robotDto) throws Exception{
        if (null == robotDto
                || null == robotDto.getRobotStatus()
        ) ExceptionCast.castFail("参数不全");
        return robotServer.robotStatus(robotDto);
    }

    @ApiOperation("补单")
    @GetMapping("/repayOrder")
    public ResponseResult repayOrder(@RequestParam Long id) throws Exception{
        if ( null == id) {
            ExceptionCast.castFail("未传入recordId");
        }
        return packetServer.repay(id);
    }

    @ApiOperation("从新初始化布隆过滤器")
    @GetMapping("/initBloomFilter")
    public void initBloomFilter() throws Exception{
        packetServer.initBloomFilter();
    }

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('query_salar')")
    public String test(){
        return  "测试成功";
    }
}
