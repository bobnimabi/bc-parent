package com.bc.manager.redPacket.client;

import com.bc.common.client.BcServiceList;
import com.bc.common.response.ResponseResult;
import com.bc.service.redPacket.dto.RobotLoginDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = BcServiceList.BC_REDPACKET_SERVER,path = "/redPacket")
public interface RedPacketClient {
    //补单
    @PostMapping("/repayOrder")
    public ResponseResult repayOrder(@RequestParam Long id) throws Exception;

    //
    @PostMapping("/robotLogin")
    public ResponseResult robotLogin(@RequestBody RobotLoginDto robotLoginDto) throws Exception;
}