package com.bc.service.redPacket.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/adjust")
public class AdjustController {

    @GetMapping("/test")
    public String test(){
        return  "测试成功";
    }


}
