package com.bc.manager.redPacket.client;

import com.bc.common.client.BcServiceList;
import com.bc.common.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = BcServiceList.BC_REDPACKET_SERVER,path = "/redPacket")
public interface RedPacketClient {
    //根据账号查询用户信息
    @PostMapping("/repay")
    public ResponseResult repay(@RequestParam Long id) throws Exception;
}