package com.njupt.swg.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.clients.ProductClient;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.BigDecimalUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.common.utils.PropertiesUtil;
import com.njupt.swg.dao.CartMapper;
import com.njupt.swg.entity.Cart;
import com.njupt.swg.entity.Product;
import com.njupt.swg.vo.CartProductVo;
import com.njupt.swg.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author swg.
 * @Date 2019/1/3 21:36
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class CartServiceImpl implements ICartService{
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private CommonCacheUtil commonCacheUtil;

    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        //1.校验参数
        if(userId == null){
            throw new SnailmallException("用户未登陆");
        }
        if(productId == null || count == null){
            return ServerResponse.createByErrorMessage("参数不正确");
        }
        //2.校验商品
        String productStr = commonCacheUtil.getCacheValue(Constants.PRODUCT_TOKEN_PREFIX+productId);
        Product product = null;
        if(productStr == null){
            ServerResponse response = productClient.queryProduct(productId);
            Object object = response.getData();
            String objStr = JsonUtil.obj2String(object);
            product = (Product) JsonUtil.Str2Obj(objStr,Product.class);
        }else {
            product = (Product) JsonUtil.Str2Obj(productStr,Product.class);
        }

        if(product == null){
            return ServerResponse.createByErrorMessage("商品不存在");
        }
        if(!product.getStatus().equals(Constants.Product.PRODUCT_ON)){
            return ServerResponse.createByErrorMessage("商品下架或者删除");
        }
        //3.根据商品或者购物车，购物车存在则增加商品数量即可，不存在则创建新的购物车，一个用户对应一个购物车
        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if (cart == null){
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Constants.Cart.CHECKED);

            int resultCount = cartMapper.insert(cartItem);
            if(resultCount == 0){
                return ServerResponse.createByErrorMessage("添加购物车失败");
            }
        }else {
            cart.setQuantity(cart.getQuantity()+count);
            int resultCount = cartMapper.updateByPrimaryKeySelective(cart);
            if(resultCount == 0){
                return ServerResponse.createByErrorMessage("添加购物车失败");
            }
        }
        //构建购物车信息，返回给前端，并且要检查库存
        CartVo cartVo = getCartVoLimit(userId,true);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse update(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if(cart == null){
            return ServerResponse.createByErrorMessage("购物车不存在");
        }
        cart.setQuantity(count);
        int updateCount = cartMapper.updateByPrimaryKeySelective(cart);
        if(updateCount == 0){
            return ServerResponse.createByErrorMessage("更新购物车失败");
        }
        CartVo cartVo = this.getCartVoLimit(userId,true);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse delete(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        int rowCount = cartMapper.deleteByProductIds(userId,productIdList);
        if(rowCount == 0){
            return ServerResponse.createByErrorMessage("此商品已经不存在于购物车中，请勿重复删除");
        }
        CartVo cartVo = this.getCartVoLimit(userId,false);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId,false);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse selectOrUnSelect(Integer userId, int checked, Integer productId) {
        cartMapper.selectOrUnSelectProduct(userId,checked,productId);
        CartVo cartVo = this.getCartVoLimit(userId,false);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<Integer> get_cart_product_count(Integer userId) {
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    @Override
    public ServerResponse removeCart(Integer userId) {
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        for(Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
        return ServerResponse.createBySuccessMessage("清除购物车成功");
    }

    /**
     * 比较通用的构建购物车的方法
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId,boolean isJudgeStock) {
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            //1.遍历购物车，一条购物车记录对应一个商品，这些购物车共同对应到一个用户userId
            for(Cart cart:cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cart.getId());
                cartProductVo.setUserId(cart.getUserId());
                cartProductVo.setProductId(cart.getProductId());
                //2.从redis中获取商品，获取不到则feign获取并且重置进redis中
                String productStr = commonCacheUtil.getCacheValue(Constants.PRODUCT_TOKEN_PREFIX+cart.getProductId());
                Product product = null;
                if(productStr == null){
                    ServerResponse response = productClient.queryProduct(cart.getProductId());
                    Object object = response.getData();
                    String objStr = JsonUtil.obj2String(object);
                    product = (Product) JsonUtil.Str2Obj(objStr,Product.class);
                }else {
                    product = (Product) JsonUtil.Str2Obj(productStr,Product.class);
                }

                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //3.判断这个商品的库存,有些接口不需要再去判断库存了，所以根据传进来的isJudgeStock这个boolean参数来决定是否判断库存
                    int buyLimitCount = 0;
                    if (isJudgeStock){
                        if(product.getStock() > cart.getQuantity()){
                            //4.库存是够的
                            buyLimitCount = cart.getQuantity();
                            cartProductVo.setLimitQuantity(Constants.Cart.LIMIT_NUM_SUCCESS);
                        }else {
                            //5.库存不够了,则返回当前最大库存
                            buyLimitCount = product.getStock();
                            cartProductVo.setLimitQuantity(Constants.Cart.LIMIT_NUM_FAIL);
                            Cart cartItem = new Cart();
                            cartItem.setId(cart.getId());
                            cartItem.setQuantity(buyLimitCount);
                            cartMapper.updateByPrimaryKeySelective(cartItem);
                        }
                    }else {
                        buyLimitCount = cart.getQuantity();
                    }

                    //6.购买的数量已经是确定的了，下面就可以直接计算价格了
                    cartProductVo.setQuantity(buyLimitCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),buyLimitCount));
                    cartProductVo.setProductChecked(cart.getChecked());
                }
                //7.选中的，就加入到总价中
                if(cart.getChecked() == Constants.Cart.CHECKED){
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.oursnail.cn/"));
        log.info("购物车列表内容为：{}",cartVo);
        return cartVo;
    }

    /**
     * 0-未勾选，1-已勾选，所以我就找有没有未勾选的商品，找到就说明没有全选
     */
    private Boolean getAllCheckedStatus(Integer userId) {
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartCheckedStatusByUserId(userId) == 0;
    }
}
