package com.njupt.swg.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2018/12/31 15:13
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/test")
@RefreshScope
public class TestController {
    @Value("${test.name}")
    private String test;

    @RequestMapping("")
    public String test(){
        return test;
    }
}
