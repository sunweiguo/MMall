package com.njupt.swg.clients;

import com.njupt.swg.common.resp.ServerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author swg.
 * @Date 2019/1/6 20:16
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("product-service")
public interface ProductClient {
    @RequestMapping("/product/queryProduct.do")
    ServerResponse queryProduct(@RequestParam("productId") Integer productId);
}