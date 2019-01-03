package com.njupt.swg.dao;

import com.njupt.swg.entity.Order;
import com.njupt.swg.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByUserIdOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectByUserId(Integer userId);

    List<OrderItem> getByOrderNo(Long orderNo);

    List<Order> selectAllOrder();

    //定时关单
    List<Order> selectOrderStatusByCreateTime(@Param("status") Integer status, @Param("date") String date);

    //关闭订单
    int closeOrderByOrderId(Integer id);
}