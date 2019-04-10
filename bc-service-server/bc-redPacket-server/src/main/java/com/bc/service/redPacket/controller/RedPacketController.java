package com.bc.service.redPacket.controller;


import com.bc.common.response.ResponseResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/")
public class RedPacketController {
    @ApiOperation("抽红包")
    @PostMapping("/updateActive")
    public ResponseResult updateActive() throws Exception{
        return null;
    }






















    @GetMapping("/test")
    @PreAuthorize("hasAuthority('query_salar')")
    public String test(){
        return  "测试成功";
    }
}
