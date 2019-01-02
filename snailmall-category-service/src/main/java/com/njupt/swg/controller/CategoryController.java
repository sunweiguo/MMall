package com.njupt.swg.controller;

import com.njupt.swg.entity.Category;
import com.njupt.swg.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/2 12:57
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;

    @RequestMapping("")
    public Category test(){
        return categoryService.test();
    }
}
