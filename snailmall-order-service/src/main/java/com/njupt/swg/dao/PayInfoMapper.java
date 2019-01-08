package com.njupt.swg.dao;

import com.njupt.swg.entity.PayInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author swg.
 * @Date 2019/1/8 10:58
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Mapper
public interface PayInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo record);

    int insertSelective(PayInfo record);

    PayInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PayInfo record);

    int updateByPrimaryKey(PayInfo record);
}
