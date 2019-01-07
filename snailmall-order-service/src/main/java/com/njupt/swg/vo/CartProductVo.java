package com.njupt.swg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author swg.
 * @Date 2019/1/5 15:18
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartProductVo implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;//购物车中此商品的数量
    private String productName;
    private String productSubtitle;
    private String productMainImage;
    private BigDecimal productPrice;
    private Integer productStatus;
    private BigDecimal productTotalPrice;
    private Integer productStock;
    private Integer productChecked;//此商品是否勾选

    private String limitQuantity;//限制数量的一个返回结果
}
