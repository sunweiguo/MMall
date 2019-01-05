package com.njupt.swg.service;

import com.github.pagehelper.PageInfo;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Order;
import com.njupt.swg.vo.OrderVo;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public interface IOrderService {
    /***后台订单管理 start***/

    /***获取订单列表，分页***/
    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    /***获取订单详情***/
    ServerResponse<OrderVo> manageDetail(Long orderNo);

    /***获取订单***/
    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    /***发货***/
    ServerResponse<String> manageSendGoods(Long orderNo);

    /***后台订单管理 end***/



}
