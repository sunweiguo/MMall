package com.njupt.swg.controller;

import com.njupt.swg.entity.Cart;
import com.njupt.swg.service.ICartService;
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
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @RequestMapping("test")
    public Cart test(){
        return cartService.test();
    }
}
