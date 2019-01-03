package com.njupt.swg.controller;

import com.njupt.swg.entity.Order;
import com.njupt.swg.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @RequestMapping("test")
    public Order test(){
        return orderService.test();
    }
}
