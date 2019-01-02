package com.njupt.swg.controller;

import com.njupt.swg.entity.Product;
import com.njupt.swg.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/2 17:32
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductService productService;

    @RequestMapping("test")
    public Product test(){
        return productService.test(26);
    }
}
