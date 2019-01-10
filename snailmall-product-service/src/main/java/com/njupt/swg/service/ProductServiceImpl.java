package com.njupt.swg.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.clients.CategoryClient;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.DateTimeUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.common.utils.PropertiesUtil;
import com.njupt.swg.dao.ProductMapper;
import com.njupt.swg.entity.Category;
import com.njupt.swg.entity.Product;
import com.njupt.swg.vo.ProductDetailVo;
import com.njupt.swg.vo.ProductListVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private CategoryClient categoryClient;
    @Autowired
    private CommonCacheUtil commonCacheUtil;


    @Override
    public ServerResponse list(int pageNum, int pageSize) {
        //1.pagehelper对下一行取出的集合进行分页
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //2.返回给前端的还需要一些其他的分页信息，为了不丢失这些信息，需要进行下面的处理
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> search(String productName, Integer productId, int pageNum, int pageSize) {
        //开始准备分页
        PageHelper.startPage(pageNum,pageSize);
        //如果有内容，可以先在这里封装好，直接传到sql中去
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectProductByNameAndId(productName,productId);
        //转换一下传给前端显示
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        //1，校验参数
        if(productId == null){
            throw new SnailmallException("参数不正确");
        }
        //1-在售 2-下架 3-删除
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("商品不存在");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<String> set_sale_status(Integer productId, Integer status) {
        //1.校验参数
        if(productId == null || status == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        //2.更新状态
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        //3.删除该商品缓存
        commonCacheUtil.delKey(Constants.PRODUCT_TOKEN_PREFIX);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_PREFIX+productId,JsonUtil.obj2String(product),Constants.PRODUCT_EXPIRE_TIME);
            return ServerResponse.createBySuccessMessage("更新产品状态成功");
        }
        return ServerResponse.createByErrorMessage("更新产品状态失败");
    }

    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        //1.校验参数
        if(product == null || product.getCategoryId()==null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        //2.设置一下主图，主图为子图的第一个图
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[] subImages = product.getSubImages().split(",");
            if(subImages.length > 0){
                product.setMainImage(subImages[0]);
            }
        }
        //3.看前端传过来的产品id是否存在，存在则为更新，否则为新增
        if(product.getId() != null){
            //删除该商品缓存
            commonCacheUtil.delKey(Constants.PRODUCT_TOKEN_PREFIX);
            int rowCount = productMapper.updateByPrimaryKeySelective(product);
            if(rowCount > 0){
                commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_PREFIX+product.getId(),JsonUtil.obj2String(product),Constants.PRODUCT_EXPIRE_TIME);
                return ServerResponse.createBySuccessMessage("更新产品成功");
            }
            return ServerResponse.createByErrorMessage("更新产品失败");
        }else {
            //新增
            product.setCreateTime(new Date());//这两句可能多余，因为xml中已经保证了，就先放这里
            product.setUpdateTime(new Date());
            int rowCount = productMapper.insert(product);
            if(rowCount > 0){
                commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_PREFIX+product.getId(),JsonUtil.obj2String(product),Constants.PRODUCT_EXPIRE_TIME);
                return ServerResponse.createBySuccessMessage("新增产品成功");
            }
            return ServerResponse.createByErrorMessage("新增产品失败");
        }
    }



    @Override
    public ServerResponse<ProductDetailVo> getPortalProductDetail(Integer productId) {
        if(productId == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("商品不存在");
        }
        if(product.getStatus() != Constants.Product.PRODUCT_ON){
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> portalList(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize) {
        //准备盛放categoryIds
        List<Integer> categoryIdList = Lists.newArrayList();
        //如果categoryId不为空
        if(categoryId != null){
            //对于这里，直接强转出错了，所以我就序列化处理了一下
            ServerResponse response = categoryClient.getCategoryDetail(categoryId);
            Object object = response.getData();
            String objStr = JsonUtil.obj2String(object);
            Category category = JsonUtil.Str2Obj(objStr,Category.class);
            if(category == null && StringUtils.isBlank(keyword)){
                ////直接返回空
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //说明category还是存在的
            categoryIdList = (List<Integer>) categoryClient.getDeepCategory(categoryId).getData();
        }
        //如果keyword不为空
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        //如果orderBy不为空
        if(StringUtils.isNotBlank(orderBy)){
            if(Constants.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                //特定的格式
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        PageHelper.startPage(pageNum,pageSize);
        //模糊查询
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,
                                                                             categoryIdList.size()==0?null:categoryIdList);
        //封装返回对象
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //返回
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse queryProduct(Integer productId) {
        //1.校验参数
        if(productId == null){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //2.去redis中查询，没有则把商品重新添加进redis中
        String redisProductStr = commonCacheUtil.getCacheValue(Constants.PRODUCT_TOKEN_PREFIX+productId);
        if (redisProductStr == null){
            Product product = productMapper.selectByPrimaryKey(productId);
            if(product == null){
                return ServerResponse.createByErrorMessage("商品不存在");
            }
            if(product.getStatus() != Constants.Product.PRODUCT_ON){
                return ServerResponse.createByErrorMessage("商品已经下架或者删除");
            }
            commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_PREFIX+productId,JsonUtil.obj2String(product),Constants.PRODUCT_EXPIRE_TIME);
        }

        //2.获取商品
        Product product = JsonUtil.Str2Obj(commonCacheUtil.getCacheValue(Constants.PRODUCT_TOKEN_PREFIX+productId),Product.class);
        return ServerResponse.createBySuccess(product);
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
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.snail.com/"));
        return productListVo;
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.snail.com/"));
        //返回给前端还需要一下该商品所处品类的父品类id，所以需要去品类服务中去查询一下，这里就要用到Feign
        if(categoryClient.getCategoryDetail(product.getCategoryId()).isSuccess()){
            ServerResponse response = categoryClient.getCategoryDetail(product.getCategoryId());
            Object object = response.getData();
            String objStr = JsonUtil.obj2String(object);
            Category category = (Category) JsonUtil.Str2Obj(objStr,Category.class);
            productDetailVo.setParentCategoryId(category.getParentId());
        }else {
            productDetailVo.setParentCategoryId(0);
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }



    @Override
    public ServerResponse preInitProductStcokToRedis() {
        List<Product> productList = productMapper.selectList();
        for(Product product:productList){
            Integer productId = product.getId();
            Integer stock = product.getStock();
            if(productId != null && stock != null && product.getStatus().equals(Constants.Product.PRODUCT_ON)){
                commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_STOCK_PREFIX+String.valueOf(productId),String.valueOf(stock),Constants.PRODUCT_EXPIRE_TIME);
            }
        }
        return ServerResponse.createBySuccessMessage("预置库存成功");
    }

    @Override
    public ServerResponse preInitProductListToRedis() {
        List<Product> productList = productMapper.selectList();
        for(Product product:productList){
            Integer productId = product.getId();
            if(productId != null  && product.getStatus().equals(Constants.Product.PRODUCT_ON)){
                commonCacheUtil.cacheNxExpire(Constants.PRODUCT_TOKEN_PREFIX+String.valueOf(productId),JsonUtil.obj2String(product),Constants.PRODUCT_EXPIRE_TIME);
            }
        }
        return ServerResponse.createBySuccessMessage("预置商品信息成功");
    }


}
