package com.njupt.swg.service;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.dao.CartMapper;
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
    public ServerResponse add(Integer id, Integer productId, Integer count) {
        return null;
    }

    @Override
    public ServerResponse update(Integer id, Integer productId, Integer count) {
        return null;
    }

    @Override
    public ServerResponse delete(Integer id, String productIds) {
        return null;
    }

    @Override
    public ServerResponse list(Integer id) {
        return null;
    }

    @Override
    public ServerResponse selectOrUnSelect(Integer id, int checked, Object o) {
        return null;
    }

    @Override
    public ServerResponse<Integer> get_cart_product_count(Integer id) {
        return null;
    }
}
