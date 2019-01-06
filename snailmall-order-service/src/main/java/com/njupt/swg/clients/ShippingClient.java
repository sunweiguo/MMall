package com.njupt.swg.clients;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Shipping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author swg.
 * @Date 2019/1/5 22:01
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("shipping-service")
public interface ShippingClient {

    @RequestMapping("/shipping/getShipping.do")
    ServerResponse getShipping(@RequestParam("shippingId") Integer shippingId);

}
