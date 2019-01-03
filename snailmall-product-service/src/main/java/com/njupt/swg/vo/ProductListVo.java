package com.njupt.swg.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author swg.
 * @Date 2019/1/3 12:06
 * @CONTACT 317758022@qq.com
 * @DESC 展示商品列表时只需要以下的属性即可
 */
@Data
public class ProductListVo {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private BigDecimal price;

    private Integer status;

    private String imageHost;
}
