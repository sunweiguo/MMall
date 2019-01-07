package com.njupt.swg.controller;

import com.github.pagehelper.PageInfo;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.Product;
import com.njupt.swg.service.IProductService;
import com.njupt.swg.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/2 17:32
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductService productService;

    @RequestMapping("detail.do")
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return productService.getPortalProductDetail(productId);
    }

    @RequestMapping("list.do")
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "")String orderBy){
        return productService.portalList(keyword,categoryId,orderBy,pageNum,pageSize);
    }

    @RequestMapping("/queryProduct.do")
    public ServerResponse queryProduct(@RequestParam("productId") Integer productId){
        return productService.queryProduct(productId);
    }

    /**
     * 补充接口1：预置每个商品库存到redis中
     */
    @RequestMapping("/preInitProductStcokToRedis.do")
    public ServerResponse preInitProductStcokToRedis(){
        return productService.preInitProductStcokToRedis();
    }


    /**
     * 补充接口2：预置所有商品到redis中
     */
    @RequestMapping("/preInitProductListToRedis.do")
    public ServerResponse preInitProductListToRedis(){
        return productService.preInitProductListToRedis();
    }


}
