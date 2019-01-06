package com.njupt.swg.service;

import com.github.pagehelper.PageInfo;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Shipping;

/**
 * @Author swg.
 * @Date 2019/1/5 19:48
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

    ServerResponse getShippingById(Integer userId,Integer shippingId);
}
