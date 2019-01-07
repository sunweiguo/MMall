package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ResponseEnum;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.ICartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC 购物车模块
 */
@RestController
@RequestMapping("/cart")
public class CartController extends BaseController{
    @Autowired
    private ICartService cartService;
    @Autowired
    private CommonCacheUtil commonCacheUtil;

    /**
     * 1.购物车添加商品
     * 超过数量会返回这样的标识"limitQuantity"
     * 失败的：LIMIT_NUM_FAIL 成功的：LIMIT_NUM_SUCCESS
     */
    @RequestMapping("add.do")
    public ServerResponse add(HttpServletRequest httpServletRequest, Integer productId, Integer count){
        User user = getCurrentUser(httpServletRequest);

        return cartService.add(user.getId(),productId,count);
    }

    /**
     * 2.更新购物车某个产品数量
     * 超过数量会返回这样的标识"limitQuantity"
     * 失败的：LIMIT_NUM_FAIL 成功的：LIMIT_NUM_SUCCESS
     */
    @RequestMapping("update.do")
    public ServerResponse update(HttpServletRequest httpServletRequest,Integer productId,Integer count){
        User user = getCurrentUser(httpServletRequest);

        return cartService.update(user.getId(),productId,count);
    }

    /**
     * 3.移除购物车某个产品
     */
    @RequestMapping("delete_product.do")
    public ServerResponse delete_product(HttpServletRequest httpServletRequest,String productIds){
        User user = getCurrentUser(httpServletRequest);

        return cartService.delete(user.getId(),productIds);
    }

    /**
     * 4.购物车List列表
     * 价格的单位是元,保留小数后2位
     */
    @RequestMapping("list.do")
    public ServerResponse list(HttpServletRequest httpServletRequest){
        User user = getCurrentUser(httpServletRequest);

        return cartService.list(user.getId());
    }


    /**
     * 5.购物车全选
     */
    @RequestMapping("select_all.do")
    public ServerResponse select_all(HttpServletRequest httpServletRequest){
        User user = getCurrentUser(httpServletRequest);

        return cartService.selectOrUnSelect(user.getId(), Constants.Cart.CHECKED,null);
    }

    /**
     * 6.购物车全不选
     */
    @RequestMapping("un_select_all.do")
    public ServerResponse un_select_all(HttpServletRequest httpServletRequest){
        User user = getCurrentUser(httpServletRequest);

        return cartService.selectOrUnSelect(user.getId(),Constants.Cart.UN_CHECKED,null);
    }

    /**
     * 7.购物车选中某个商品
     */
    @RequestMapping("select.do")
    public ServerResponse select(HttpServletRequest httpServletRequest,Integer productId){
        User user = getCurrentUser(httpServletRequest);

        return cartService.selectOrUnSelect(user.getId(),Constants.Cart.CHECKED,productId);
    }

    /**
     * 8.购物车取消选中某个商品
     */
    @RequestMapping("un_select.do")
    public ServerResponse un_select(HttpServletRequest httpServletRequest,Integer productId){
        User user = getCurrentUser(httpServletRequest);

        return cartService.selectOrUnSelect(user.getId(),Constants.Cart.UN_CHECKED,productId);
    }


    /**
     * 9.查询在购物车里的产品数量
     */
    @RequestMapping("get_cart_product_count.do")
    public ServerResponse<Integer> get_cart_product_count(HttpServletRequest httpServletRequest){
        User user = getCurrentUser(httpServletRequest);

        return cartService.get_cart_product_count(user.getId());
    }


    /**
     * 提供的feign接口，根据userId获取购物车列表
     */
    @RequestMapping("getCartList.do")
    public ServerResponse getCartList(HttpServletRequest httpServletRequest){
        User user = null;
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if(name.equalsIgnoreCase("snailmall_login_token")){
                    String value = httpServletRequest.getHeader(name);
                    if(StringUtils.isBlank(value)){
                        return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
                    }
                    String userJsonStr = commonCacheUtil.getCacheValue(value);
                    if(userJsonStr == null){
                        return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
                    }
                    user = JsonUtil.Str2Obj(userJsonStr,User.class);
                }
            }
        }
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
        }
        return cartService.list(user.getId());
    }

    /**
     * 提供的feign接口，清空购物车
     */
    @RequestMapping("removeCart.do")
    public ServerResponse removeCart(HttpServletRequest httpServletRequest){
        User user = null;
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if(name.equalsIgnoreCase("snailmall_login_token")){
                    String value = httpServletRequest.getHeader(name);
                    if(StringUtils.isBlank(value)){
                        return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
                    }
                    String userJsonStr = commonCacheUtil.getCacheValue(value);
                    if(userJsonStr == null){
                        return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
                    }
                    user = JsonUtil.Str2Obj(userJsonStr,User.class);
                }
            }
        }
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆，无法获取当前用户信息");
        }
        return cartService.removeCart(user.getId());
    }



}
