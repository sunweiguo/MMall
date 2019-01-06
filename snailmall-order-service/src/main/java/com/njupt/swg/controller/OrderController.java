package com.njupt.swg.controller;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author swg.
 * @Date 2019/1/6 19:53
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/order/")
public class OrderController extends BaseController{
    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单
     */
    @RequestMapping("create.do")
    public ServerResponse create(HttpServletRequest httpServletRequest, Integer shippingId){
        User user = getCurrentUser(httpServletRequest);
        return orderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     */
    @RequestMapping("cancel.do")
    public ServerResponse cancel(HttpServletRequest httpServletRequest, Long orderNo){
        User user = getCurrentUser(httpServletRequest);
        return orderService.cancel(user.getId(),orderNo);
    }

    /**
     * 获取订单商品信息
     */
    @RequestMapping("get_order_cart_product.do")
    public ServerResponse getOrderCartProduct(HttpServletRequest httpServletRequest){
        User user = getCurrentUser(httpServletRequest);
        return orderService.getOrderCartProduct(user.getId());
    }

    /**
     * 订单详情
     */
    @RequestMapping("detail.do")
    public ServerResponse detail(HttpServletRequest httpServletRequest,Long orderNo){
        User user = getCurrentUser(httpServletRequest);
        return orderService.getOrderDetail(user.getId(),orderNo);
    }

    /**
     * 订单列表
     */
    @RequestMapping("list.do")
    public ServerResponse list(HttpServletRequest httpServletRequest, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = getCurrentUser(httpServletRequest);
        return orderService.getOrderList(user.getId(),pageNum,pageSize);
    }

}
