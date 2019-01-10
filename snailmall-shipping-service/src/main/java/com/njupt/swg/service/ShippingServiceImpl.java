package com.njupt.swg.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.dao.ShippingMapper;
import com.njupt.swg.entity.Shipping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author swg.
 * @Date 2019/1/5 19:52
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class ShippingServiceImpl implements IShippingService{
    @Autowired
    private ShippingMapper shippingMapper;


    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        if(shippingId == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        int resultCount = shippingMapper.deleteByUserIdShippingId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    @Override
    public ServerResponse select(Integer userId, Integer shippingId) {
        if(shippingId == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("查询地址成功",shipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse getShippingById(Integer userId,Integer shippingId) {
        if(shippingId == null){
            log.error("【shippingId为空】");
           return ServerResponse.createByErrorMessage("参数不正确");
        }
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if(shipping == null){
            log.error("【获取地址失败】");
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        log.info("【获取地址成功：{}】",shipping);
        return ServerResponse.createBySuccess("获取地址成功",shipping);
    }
}
