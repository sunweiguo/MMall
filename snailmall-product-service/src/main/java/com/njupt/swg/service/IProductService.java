package com.njupt.swg.service;

import com.github.pagehelper.PageInfo;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Product;
import com.njupt.swg.vo.ProductDetailVo;

/**
 * @Author swg.
 * @Date 2019/1/2 17:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public interface IProductService {
    /** 后台获取产品分页列表 **/
    ServerResponse list(int pageNum, int pageSize);

    /** 后台的搜索，根据id或者name模糊查询 **/
    ServerResponse<PageInfo> search(String productName, Integer productId, int pageNum, int pageSize);

    /**  后台查看商品详情 **/
    ServerResponse<ProductDetailVo> detail(Integer productId);

    /** 后台设置商品的上下架状态 **/
    ServerResponse<String> set_sale_status(Integer productId, Integer status);

    /** 后台新增或者更新商品 **/
    ServerResponse<String> saveOrUpdateProduct(Product product);

    /** 前台门户获取产品详情 **/
    ServerResponse<ProductDetailVo> getPortalProductDetail(Integer productId);

    /** 前台门户获取商品分页列表 **/
    ServerResponse<PageInfo> portalList(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize);

    /** 从redis中查询商品，没有则将商品放进redis中 **/
    ServerResponse queryProduct(Integer productId);

    /**  补充接口1：预置商品库存到redis **/
    ServerResponse preInitProductStcokToRedis();

    /**  补充接口2：预置商品列表信息到redis **/
    ServerResponse preInitProductListToRedis();
}
