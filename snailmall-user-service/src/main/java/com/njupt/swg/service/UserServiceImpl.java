package com.njupt.swg.service;

import com.njupt.swg.dao.UserMapper;
import com.njupt.swg.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author swg.
 * @Date 2018/12/31 21:08
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(int id) {
        return userMapper.selectByPrimaryKey(1);
    }
}
