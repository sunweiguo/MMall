package com.njupt.swg.service;

import com.njupt.swg.dao.OrderMapper;
import com.njupt.swg.entity.Order;
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
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Order test() {
        return orderMapper.selectByPrimaryKey(103);
    }
}
