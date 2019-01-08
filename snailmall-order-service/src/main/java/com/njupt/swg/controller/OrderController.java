package com.njupt.swg.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ResponseEnum;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author swg.
 * @Date 2019/1/6 19:53
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/order/")
@Slf4j
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



    @RequestMapping("pay.do")
    public ServerResponse pay(Long orderNo, HttpServletRequest request){
        User user = getCurrentUser(request);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),ResponseEnum.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(user.getId(),orderNo,path);
    }

    @RequestMapping("query_order_pay_status.do")
    public ServerResponse<Boolean> query_order_pay_status(Long orderNo,HttpServletRequest request){
        User user = getCurrentUser(request);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),ResponseEnum.NEED_LOGIN.getDesc());
        }
        ServerResponse response = orderService.query_order_pay_status(user.getId(),orderNo);
        if(response.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

    @RequestMapping("alipay_callback.do")
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i = 0 ; i <values.length;i++){

                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i]+",";
            }
            params.put(name,valueStr);
        }
        log.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //根据后来的日志打印，输出结果是：
        /**
         * 支付宝回调,
         sign:eT1uLKs0Wh9/LlqdGkPVkCAo/Di2EuI+Fu0Du6FlFbLx7vJn4ZhKUHEiYqklz3Rrfjmz4+8qMSaiLG8vhAIT6j20rcUrDYWhHVngbMJJRamrT0IN6o9f6QwI/u7VZvJq6CBFvPs9Axkfl61UwSDItgi/Tqf9sumRuLjK/brNUy6IURBr6SazFoq7gTSQOG8c8b5mLyPF8lWkjzD8g8LyVpTETYSiRA4W5ySI677tSO3I6H+BawDQQzjErQwko9i83t+Wc63JRNMYi4sFrYREsRfLJYSp684YAXfyIF17nm0rBXK9tUk9SIYIDrSkZEJjxkxAQgZuFjHwE1BVOxqmcQ==,

         trade_status:TRADE_SUCCESS,

         参数:
         {gmt_create=2018-01-12 14:43:43,
         charset=utf-8,
         seller_email=qqojiw4740@sandbox.com,
         subject=快乐蜗牛商城扫码支付,
         订单号:1492091089794,
         sign=eT1uLKs0Wh9/LlqdGkPVkCAo/Di2EuI+Fu0Du6FlFbLx7vJn4ZhKUHEiYqklz3Rrfjmz4+8qMSaiLG8vhAIT6j20rcUrDYWhHVngbMJJRamrT0IN6o9f6QwI/u7VZvJq6CBFvPs9Axkfl61UwSDItgi/Tqf9sumRuLjK/brNUy6IURBr6SazFoq7gTSQOG8c8b5mLyPF8lWkjzD8g8LyVpTETYSiRA4W5ySI677tSO3I6H+BawDQQzjErQwko9i83t+Wc63JRNMYi4sFrYREsRfLJYSp684YAXfyIF17nm0rBXK9tUk9SIYIDrSkZEJjxkxAQgZuFjHwE1BVOxqmcQ==,
         body=订单1492091089794购买商品共6999.00元,
         buyer_id=2088102175449759,
         invoice_amount=6999.00,
         notify_id=d4832db3a886f294164df8872927e82lse,
         fund_bill_list=[{"amount":"6999.00","fundChannel":"ALIPAYACCOUNT"}],
         notify_type=trade_status_sync,
         trade_status=TRADE_SUCCESS,
         receipt_amount=6999.00,
         app_id=2016082100306095,
         buyer_pay_amount=6999.00,
         sign_type=RSA2,
         seller_id=2088102172437193,
         gmt_payment=2018-01-12 14:43:48,
         notify_time=2018-01-12 14:43:49,
         version=1.0,
         out_trade_no=1492091089794,
         total_amount=6999.00,
         trade_no=2018011221001004750200171585,
         auth_app_id=2016082100306095,
         buyer_logon_id=uaa***@sandbox.com,
         point_amount=0.00}
         */

        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.

        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());

            if(!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求,验证不通过,再恶意请求我就报警找网警了");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验证回调异常",e);
        }

        ServerResponse serverResponse = orderService.aliCallback(params);
        if(serverResponse.isSuccess()){
            return Constants.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Constants.AlipayCallback.RESPONSE_FAILED;
    }

}
