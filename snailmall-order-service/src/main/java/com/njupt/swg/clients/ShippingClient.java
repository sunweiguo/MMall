package com.njupt.swg.clients;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Shipping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author swg.
 * @Date 2019/1/5 22:01
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("shipping-service")
public interface ShippingClient {

    @RequestMapping("/shipping/getShipping.do")
    Shipping getShipping(Integer shippingId);

}
