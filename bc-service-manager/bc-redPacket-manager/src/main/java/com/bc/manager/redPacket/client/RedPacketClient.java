package com.bc.manager.redPacket.client;

import com.bc.common.client.BcServiceList;
import com.bc.common.response.ResponseResult;
import com.bc.manager.redPacket.dto.IdList;
import com.bc.manager.redPacket.dto.IdLongList;
import com.bc.manager.redPacket.dto.VsRobotDto;
import com.bc.service.redPacket.dto.RobotLoginDto;
import com.bc.utils.MyHttpResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@FeignClient(value = BcServiceList.BC_REDPACKET_SERVER,path = "/redPacket")
public interface RedPacketClient {
    @ApiOperation("补单")
    @PostMapping("/repayOrder")
    public ResponseResult repayOrder(@RequestBody IdLongList idList) throws Exception;

    @ApiOperation("机器人登录")
    @PostMapping("/robotLogin")
    public ResponseResult robotLogin(@RequestBody RobotLoginDto robotLoginDto) throws Exception;

    @ApiOperation("获取图片验证码")
    @GetMapping("/getVarCode")
    public byte[] getImageCode(@RequestParam Integer robotNum) throws Exception;

    @ApiOperation("机器人增加")
    @PostMapping("/addRobot")
    public ResponseResult addRobot(@RequestBody VsRobotDto robotDto) throws Exception;

    @ApiOperation("机器人开启或关闭")
    @PostMapping("/robotStatus")
    public ResponseResult robotStatus(@RequestBody VsRobotDto robotDto) throws Exception;

    @ApiOperation("机器人：修改")
    @PostMapping("/updateRobot")
    public ResponseResult updateRobot(@RequestBody VsRobotDto robotDto) throws Exception;

    @ApiOperation("从新初始化布隆过滤器")
    @GetMapping("/initBloomFilter")
    public void initBloomFilter() throws Exception;
}