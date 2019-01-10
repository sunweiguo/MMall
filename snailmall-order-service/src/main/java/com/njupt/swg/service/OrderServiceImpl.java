package com.njupt.swg.service;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.clients.CartClient;
import com.njupt.swg.clients.KeyGenClient;
import com.njupt.swg.clients.ProductClient;
import com.njupt.swg.clients.ShippingClient;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ResponseEnum;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.*;
import com.njupt.swg.dao.OrderItemMapper;
import com.njupt.swg.dao.OrderMapper;
import com.njupt.swg.dao.PayInfoMapper;
import com.njupt.swg.entity.*;
import com.njupt.swg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alipay.demo.trade.model.GoodsDetail;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShippingClient shippingClient;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private CommonCacheUtil commonCacheUtil;
    @Autowired
    private KeyGenClient keyGenClient;
    @Autowired
    private PayInfoMapper payInfoMapper;

    /*** 后台订单管理 start***/

    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    @Override
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    @Override
    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);

            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    @Override
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Constants.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Constants.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("发货成功");
            }
            return ServerResponse.createByErrorMessage("发货失败");
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Constants.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Constants.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = null;
        ServerResponse response = shippingClient.getShipping(order.getShippingId());
        if(response.getStatus() == ResponseEnum.NEED_LOGIN.getCode()){
            throw new SnailmallException("用户需要重新登陆");
        }
        if(!response.isSuccess()){
            throw new SnailmallException("获取地址出现错误!");
        }else {
            Object object = response.getData();
            String objStr = JsonUtil.obj2String(object);
            shipping = JsonUtil.Str2Obj(objStr,Shipping.class);
        }

        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }else{
            throw new SnailmallException("地址没有获取成功");
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));


        orderVo.setImageHost("http://img.oursnail.cn/");


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem>  orderItemList = Lists.newArrayList();
            if(userId == null){
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /***门户订单管理 start***/
    /**
     * 创建订单
     */
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //1. TODO 对这个userId加上一个分布式锁，锁上一小段时间，防止这个用户在极短时间内重复点击下单

        log.info("【开始创建订单，userId为：{}，shippingID为：{}】",userId,shippingId);
        //2. lua脚本来判断redis中库存还有没有，并且减库存---redis预减库存
        ServerResponse response = cartClient.getCartList();
        if(response.getStatus() == ResponseEnum.NEED_LOGIN.getCode()){
            return ServerResponse.createByErrorMessage("用户登陆信息有问题，请重新登陆");
        }

        Object object = response.getData();
        String objStr = JsonUtil.obj2String(object);
        log.info("【获取到的购物车信息为：{}】",objStr);
        if(objStr == null){
            log.error("【获取购物车信息失败】");
            return ServerResponse.createByErrorMessage("获取购物车失败");
        }
        CartVo cartVo = JsonUtil.Str2Obj(objStr,CartVo.class);
        List<CartProductVo> cartProductVoList = cartVo.getCartProductVoList();
        List<MessageVo> messageVoList = new ArrayList<>();
        //这里生成订单号，方便查询
        String orderNoStr = keyGenClient.generateKey();
        orderNoStr = orderNoStr.substring(0,orderNoStr.length()-3);
        long orderNo = Long.parseLong(orderNoStr);
        log.info("【生成的订单号为:{}】",orderNo);
        for(CartProductVo cartProductVo:cartProductVoList){
            MessageVo messageVo = new MessageVo();
            Integer productId = cartProductVo.getProductId();
            Integer quantity = cartProductVo.getQuantity();
            long resultCode = (long) redisUtils.reduceStock(Constants.PRODUCT_TOKEN_STOCK_PREFIX+productId,String.valueOf(quantity));
            log.info("【lua脚本返回的数为：{}】",resultCode);
            if(resultCode == -2){
//                map.put(productId,-2);
                log.error("【商品{}库存不存在】",productId);
                continue;
            }else if(resultCode == -1){
//                map.put(productId,-1);
                log.error("【商品{}库存不足】",productId);
                continue;
            }else{
                //只把库存存在并且库存充足的商品参与订单，不符合条件的，给用户一个提示即可
                messageVo.setCartVo(cartVo);
                messageVo.setUserId(userId);
                messageVo.setShippingId(shippingId);
                messageVo.setOrderNo(orderNo);
                messageVoList.add(messageVo);
            }
        }

        //3.判断一下list是不是空的
        if(messageVoList.size() == 0){
            log.info("【商品不存在或者库存不够】");
            return ServerResponse.createByErrorMessage("商品不存在或者库存不够");
        }

        //4. 扣减库存、生成订单,参数userId,ShippingId传给MQ取异步下单
        amqpTemplate.convertAndSend("order-queue",JsonUtil.obj2String(messageVoList));

        //这里由于前端限制（不会改前端，应该是先显示排队中，所以直接就取数据库查询订单信息返回给前端）
        //这里就直接返回成功了

//        boolean flag = false;
//        do{
//
//            Order order = orderMapper.selectByOrderNo(orderNo);
//            if (order != null){
//                log.info("【订单{}生成好了，这里直接返回给前端】",order.getOrderNo());
//                flag = true;
//                break;
//            }
//            log.error("【订单{}还未生成好，继续下次循环】",orderNo);
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }while (!flag);
        log.info("【返回给前端的orderNo：{}】",orderNo);
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(orderNo);
        log.info("【返回给前端的数据为：{}】",orderVo);
        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assembleResultOrderVo(long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        return assembleOrderVo(order,orderItemList);
    }


    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus() >= Constants.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("已付款，不能取消该订单了");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Constants.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(rowCount > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        ServerResponse response = cartClient.getCartList();
        if(response.getStatus() == ResponseEnum.NEED_LOGIN.getCode()){
            return ServerResponse.createByErrorMessage("用户登陆信息有问题，请重新登陆");
        }

        Object object = response.getData();
        String objStr = JsonUtil.obj2String(object);
        log.info("【获取到的购物车信息为：{}】",objStr);
        if(objStr == null){
            return ServerResponse.createByErrorMessage("获取购物车失败");
        }
        CartVo cartVo = JsonUtil.Str2Obj(objStr,CartVo.class);
        List<CartProductVo> cartProductVoList = cartVo.getCartProductVoList();
        response = this.getCartOrderItem(userId,cartProductVoList);
        if(!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem:orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost("http://img.oursnail.cn/");
        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            log.error("【用户{}不存在{}订单】",userId,orderNo);
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        log.info("获取订单详情列表成功{}",orderItemList);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    @Transactional
    public ServerResponse createOrderProcess(List<MessageVo> resultList){
        //0.获取userId和shippingId
        log.info("【生成订单主表和订单详情表】");
        Integer userId = resultList.get(0).getUserId();
        Integer shippingId = resultList.get(0).getShippingId();
        long orderNo = resultList.get(0).getOrderNo();
        log.info("【获取userId:{}和shippingId:{}，orderNo:{}】",userId,shippingId,orderNo);

        //1.获取购物车列表
        CartVo cartVo = resultList.get(0).getCartVo();
        log.info("【获取购物车列表:{}】",cartVo);

        List<CartProductVo> cartProductVoList = cartVo.getCartProductVoList();
        //2.根据购物车构建订单详情
        ServerResponse response = this.getCartOrderItem(userId,cartProductVoList);
        if(!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        if(CollectionUtils.isEmpty(orderItemList)){
            log.error("【购物车为空】");
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //3.计算总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        log.info("【计算总价为：{}】",payment);
        //4.构建订单主表
        Order order = this.assembleOrder(userId,shippingId,payment,orderNo);
        if(order == null){
            log.error("【生成订单主表失败】");
            return ServerResponse.createByErrorMessage("生成订单失败");
        }
        for(OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(orderNo);
        }
        //4.批量插入订单详情
        orderItemMapper.batchInsert(orderItemList);

        return ServerResponse.createBySuccess("扣减库存、生成订单成功...",userId);
    }

    @Override
    public ServerResponse stockAndOrderprocess(List<MessageVo> result) {
        log.info("【开始消费，参数未{}】",result);
        ServerResponse response = this.createOrderProcess(result);
        log.info("订单生成完毕，下面就是去清空购物车，MQ异步出去");
        if(response.isSuccess()){
            Integer userId = (Integer) response.getData();
            log.info("【需要清空购物车的用户为:{}】",userId);
            amqpTemplate.convertAndSend("cart-queue",userId);
        }
        //至于数据库的扣减库存，采用定时任务去redis中去同步
        return ServerResponse.createBySuccessMessage("扣减库存、生成订单成功");
    }

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment,long orderNo){
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(Constants.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Constants.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间等等
        //付款时间等等
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }


    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem:orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse getCartOrderItem(Integer userId, List<CartProductVo> cartList) {
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        List<OrderItem> orderItemList = Lists.newArrayList();

        for(CartProductVo cart:cartList){
            OrderItem orderItem = new OrderItem();

            String productStr = commonCacheUtil.getCacheValue(Constants.PRODUCT_TOKEN_PREFIX);
            Product product = null;
            if(productStr == null){
                ServerResponse response = productClient.queryProduct(cart.getProductId());
                Object object = response.getData();
                String objStr = JsonUtil.obj2String(object);
                product = (Product) JsonUtil.Str2Obj(objStr,Product.class);
            }else {
                product = (Product) JsonUtil.Str2Obj(productStr,Product.class);
            }

            if(product == null){
                return ServerResponse.createByErrorMessage("商品不存在");
            }

            //判断产品的是否在售
            if(product.getStatus() != Constants.Product.PRODUCT_ON){
                return ServerResponse.createByErrorMessage("产品不在售卖状态");
            }
            //判断产品库存是否足够
            if(cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品库存不够");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity(),product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }







/**********************以下是关于支付的逻辑处理*****************************/






    private static AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Override
    public ServerResponse pay(Integer userId, Long orderNo, String path) {
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户没有该订单");
        }
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

        //1.获取订单号
        String outTradeNo = order.getOrderNo().toString();
        log.info("【1.获取订单号：{}】",orderNo);

        //2.订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("快乐蜗牛商城扫码支付,订单号:").append(outTradeNo).toString();
        log.info("【2.订单标题：{}】",subject);

        // 3.(必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();
        log.info("【3.订单总金额：{}】",totalAmount);

        // 4.(可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";


        // 5.卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 6.订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();
        log.info("【4.订单描述：{}】",body);

        // 7.商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // 8.(必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 9.业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");


        // 10.支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 11.商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        for(OrderItem orderItem : orderItemList){
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 12.创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl("http://www.oursnail.cn/order/order/alipay_callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        // 13.创建扫码支付请求成功后，支付宝返回一串二维码串
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //细节细节细节
                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path,qrFileName);
                try {
                    FtpUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常",e);
                }
                log.info("qrPath:" + qrPath);
                String qrUrl = "http://img.oursnail.cn/"+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    @Override
    public ServerResponse query_order_pay_status(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户没有该订单");
        }
        if(order.getStatus() >= Constants.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("非快乐蜗牛商城的订单,回调忽略");
        }
        //判断订单的状态是否大于已支付的状态，如果大于，说明是支付宝的重复通知，直接返回success即可
        if(order.getStatus() >= Constants.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        //根据支付宝的回调状态来更新订单的状态
        if(Constants.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Constants.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //插入支付信息
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Constants.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

}
