package com.njupt.swg.web;

import com.njupt.swg.keygen.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/6 21:47
 * @CONTACT 317758022@qq.com
 * @DESC 全局唯一ID生成服务
 */
@RestController
@RequestMapping
public class KeyGeneratorController {


    @Autowired
    @Qualifier("snowFlakeKeyGenerator")
    private KeyGenerator keyGenerator;

    @RequestMapping("/keygen")
    public String generateKey() throws Exception {
        return String.valueOf(keyGenerator.generateKey().longValue());
    }

}