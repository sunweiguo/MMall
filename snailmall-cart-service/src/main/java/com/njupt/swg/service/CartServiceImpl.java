package com.njupt.swg.service;

import com.njupt.swg.dao.CartMapper;
import com.njupt.swg.entity.Cart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class CartServiceImpl implements ICartService{
    @Autowired
    private CartMapper cartMapper;

    @Override
    public Cart test() {
        return cartMapper.selectByPrimaryKey(126);
    }
}
