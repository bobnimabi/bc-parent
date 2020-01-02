package com.bc.service.redPacket.temp;

import com.bc.common.response.ResponseResult;
import com.bc.service.redPacket.dto.PayMoneyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "robot-jiuwu-activity-server",path = "/jiuwuActivityRobot")
public interface JiuWuClient {
    //查询用户是否存在
    @GetMapping("/isExist")
    public ResponseResult isExist(@RequestParam String username) throws Exception;

    // 测试打款
    @PostMapping("/tempPay")
    public ResponseResult tempPay(@RequestBody PayMoneyDTO payMoneyDTO) throws Exception;
}