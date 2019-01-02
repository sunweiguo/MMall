package com.njupt.swg.service;

import com.njupt.swg.dao.ProductMapper;
import com.njupt.swg.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author swg.
 * @Date 2019/1/2 17:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class ProductServiceImpl implements IProductService{
    @Autowired
    private ProductMapper productMapper;


    @Override
    public Product test(int id) {
        return productMapper.selectByPrimaryKey(id);
    }
}
