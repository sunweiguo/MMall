package com.njupt.swg.dao;

import com.njupt.swg.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectCartCheckedStatusByUserId(Integer userId);

    int deleteByProductIds(@Param("userId") Integer userId, @Param("productIdList") List<String> productIdList);

    void selectOrUnSelectProduct(@Param("userId") Integer userId, @Param("checked") int checked, @Param("productId") Integer productId);

    int selectCartProductCount(Integer userId);

    List<Cart> selectCheckedCartByUserId(Integer userId);
}