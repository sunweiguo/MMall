package com.njupt.swg.controller;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/2 17:32
 * @CONTACT 317758022@qq.com
 * @DESC 后台商品服务
 */
@RestController
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IProductService productService;

    @RequestMapping("/list.do")
    public ServerResponse list(){
        return null;
    }
}
