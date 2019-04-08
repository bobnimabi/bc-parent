package com.bc.service.redPacket.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/")
public class RedPacketController {























    @GetMapping("/test")
    @PreAuthorize("hasAuthority('query_salar')")
    public String test(){
        return  "测试成功";
    }
}
