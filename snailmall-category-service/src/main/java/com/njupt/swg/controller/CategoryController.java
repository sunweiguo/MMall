package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.CookieUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    @Autowired
    private CommonCacheUtil commonCacheUtil;

    /**
     * 获取品类子节点(平级)
     */
    @RequestMapping("get_category.do")
    public ServerResponse getCategory(HttpServletRequest request, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        //校验是否为管理员身份
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);

        if(!currentUser.getRole().equals(Constants.Role.ROLE_ADMIN)){
            //不是管理员报错
            return ServerResponse.createByErrorMessage("权限不够");
        }

        ServerResponse response = categoryService.getCategory(categoryId);
        return response;
    }

    /**
     * 增加节点
     */
    @RequestMapping("add_category.do")
    public ServerResponse addCategory(HttpServletRequest request, String categoryName, @RequestParam(value = "parentId",defaultValue = "0")int parentId){
        //校验是否为管理员身份
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);

        if(!currentUser.getRole().equals(Constants.Role.ROLE_ADMIN)){
            //不是管理员报错
            return ServerResponse.createByErrorMessage("权限不够");
        }

        ServerResponse response = categoryService.addCategory(categoryName,parentId);
        return response;
    }

    /**
     * 修改品类名称
     */
    @RequestMapping("set_category_name.do")
    public ServerResponse<String> set_category_name(HttpServletRequest request, String categoryName,Integer categoryId){
        //校验是否为管理员身份
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);

        if(!currentUser.getRole().equals(Constants.Role.ROLE_ADMIN)){
            //不是管理员报错
            return ServerResponse.createByErrorMessage("权限不够");
        }

        return categoryService.updateCategoryName(categoryName,categoryId);
    }

    /**
     * 递归获取自身和所有的子节点
     */
    @RequestMapping("get_deep_category.do")
    public ServerResponse get_deep_category(HttpServletRequest request,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        //校验是否为管理员身份
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);

        if(!currentUser.getRole().equals(Constants.Role.ROLE_ADMIN)){
            //不是管理员报错
            return ServerResponse.createByErrorMessage("权限不够");
        }

        return categoryService.selectCategoryAndDeepChildrenById(categoryId);
    }




}
