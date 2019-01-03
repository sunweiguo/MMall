package com.njupt.swg.controller;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author swg.
 * @Date 2019/1/2 12:57
 * @CONTACT 317758022@qq.com
 * @DESC 品类接口，这属于后端管理系统人员可以操作的，所以需要管理员权限
 */

//TODO 这里首先实现业务  关于这里重复的鉴权，后面将会移植到网关中统一去做
//TODO 先开放GET请求
@RestController
@RequestMapping("/manage/category/")
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取品类子节点(平级)
     */
    @RequestMapping("get_category.do")
    public ServerResponse getCategory(@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        ServerResponse response = categoryService.getCategory(categoryId);
        return response;
    }

    /**
     * 增加节点
     */
    @RequestMapping("add_category.do")
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId",defaultValue = "0")int parentId){
        ServerResponse response = categoryService.addCategory(categoryName,parentId);
        return response;
    }

    /**
     * 修改品类名称
     */
    @RequestMapping("set_category_name.do")
    public ServerResponse<String> set_category_name(String categoryName,Integer categoryId){
        return categoryService.updateCategoryName(categoryName,categoryId);
    }

    /**
     * 递归获取自身和所有的子节点
     */
    @RequestMapping("get_deep_category.do")
    public ServerResponse get_deep_category(@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        return categoryService.selectCategoryAndDeepChildrenById(categoryId);
    }


    /**
     * 这是为了给其他服务调用而新增的接口
     */
    @RequestMapping("get_category_detail.do")
    public ServerResponse get_category_detail(Integer categoryId){
        return categoryService.getCategoryDetail(categoryId);
    }




}
