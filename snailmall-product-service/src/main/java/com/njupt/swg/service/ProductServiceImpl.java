package com.njupt.swg.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.njupt.swg.common.Parameters;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.dao.ProductMapper;
import com.njupt.swg.entity.Product;
import com.njupt.swg.vo.ProductDetailVo;
import com.njupt.swg.vo.ProductListVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Autowired
    private Parameters parameters;


    @Override
    public ServerResponse list(int pageNum, int pageSize) {
        //pagehelper对下一行取出的集合进行分页
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //返回给前端的还需要一些其他的分页信息，为了不丢失这些信息，需要进行下面的处理
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> search(String productName, Integer productId, int pageNum, int pageSize) {

        return null;
    }

    @Override
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return null;
    }

    @Override
    public ServerResponse<String> set_sale_status(Integer productId, Integer status) {
        return null;
    }

    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        return null;
    }



    @Override
    public ServerResponse<ProductDetailVo> getPortalProductDetail(Integer productId) {
        return null;
    }

    @Override
    public ServerResponse<PageInfo> portalList(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize) {
        return null;
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(parameters.getImageHost());
        return productListVo;
    }


}
