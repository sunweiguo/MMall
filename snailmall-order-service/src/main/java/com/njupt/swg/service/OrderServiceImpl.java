package com.njupt.swg.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
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
import com.njupt.swg.entity.*;
import com.njupt.swg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


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
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //1. TODO 对这个userId加上一个分布式锁，锁上一小段时间，防止这个用户在极短时间内重复点击下单

        //2. lua脚本来判断redis中库存还有没有，并且减库存---redis预减库存
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
        List<MessageVo> messageVoList = new ArrayList<>();
        //这里生成订单号，方便查询
        long orderNo = Long.parseLong(keyGenClient.generateKey());
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
                messageVoList.add(messageVo);
                messageVo.setOrderNo(orderNo);
            }
        }

        //3.判断一下list是不是空的
        if(messageVoList.size() == 0){
            return ServerResponse.createByErrorMessage("商品不存在或者库存不够");
        }

        //4. 扣减库存、生成订单,参数userId,ShippingId传给MQ取异步下单
        amqpTemplate.convertAndSend("order-queue",JsonUtil.obj2String(messageVoList));

        //这里由于前端限制（不会改前端，应该是先显示排队中，所以直接就取数据库查询订单信息返回给前端）
        //TODO 这里就直接去数据库查一下

        boolean flag = false;
        do{
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order != null){
                flag = true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (!flag);
        OrderVo orderVo = assembleResultOrderVo(orderNo);

        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assembleResultOrderVo(long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        return assembleOrderVo(order,orderItemList);
    }


    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        return null;
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        return null;
    }

    @Override
    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        return null;
    }

    @Override
    public ServerResponse getOrderList(Integer userId, int pageNum, int pageSize) {
        return null;
    }


    @Transactional
    public ServerResponse createOrderProcess(List<MessageVo> resultList){
        //0.获取userId和shippingId
        Integer userId = resultList.get(0).getUserId();
        Integer shippingId = resultList.get(0).getShippingId();
        long orderNo = resultList.get(0).getOrderNo();

        //1.获取购物车列表
        CartVo cartVo = resultList.get(0).getCartVo();

        List<CartProductVo> cartProductVoList = cartVo.getCartProductVoList();
        //2.根据购物车构建订单详情
        ServerResponse response = this.getCartOrderItem(userId,cartProductVoList);
        if(!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //3.计算总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //4.构建订单主表
        Order order = this.assembleOrder(userId,shippingId,payment,orderNo);
        if(order == null){
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
        ServerResponse response = this.createOrderProcess(result);
        if(response.isSuccess()){
            Integer userId = (Integer) response.getData();
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


}
