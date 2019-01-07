package com.njupt.swg.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author swg.
 * @Date 2019/1/7 16:35
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("KEYGEN-SERVICE")
public interface KeyGenClient {
    @RequestMapping("/keygen")
    String generateKey();
}
