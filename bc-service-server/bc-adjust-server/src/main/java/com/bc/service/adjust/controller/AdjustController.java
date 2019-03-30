package com.bc.service.adjust.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/adjust")
@Slf4j
public class AdjustController {

    @GetMapping("/test")
    public String test(){
        log.error("c嗷呜");
        log.info("测试成功了");
        return  "测试成功";
    }


}
