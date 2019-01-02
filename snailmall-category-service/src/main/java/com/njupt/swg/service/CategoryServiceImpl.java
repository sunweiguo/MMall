package com.njupt.swg.service;

import com.njupt.swg.dao.CategoryMapper;
import com.njupt.swg.entity.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author swg.
 * @Date 2019/1/2 12:54
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class CategoryServiceImpl implements ICategoryService{
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category test() {
        return categoryMapper.selectByPrimaryKey(100001);
    }
}
