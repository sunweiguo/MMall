package com.njupt.swg.dao;

import com.njupt.swg.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author swg.
 * @Date 2018/12/31 21:03
 * @CONTACT 317758022@qq.com
 * @DESC Mapper
 */
@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    Integer selectByUsername(String username);

    User selectByUsernameAndPasswd(@Param("username") String username,@Param("password") String md5Passwd);

    Integer selectByEmail(String str);
}
