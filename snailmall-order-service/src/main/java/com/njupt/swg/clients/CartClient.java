package com.njupt.swg.clients;

import com.njupt.swg.common.resp.ServerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author swg.
 * @Date 2019/1/6 20:06
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("cart-service")
public interface CartClient {
    @RequestMapping("/cart/getCartList.do")
    ServerResponse getCartList();

    @RequestMapping("/cart/removeCart.do")
    ServerResponse removeCart();

}
