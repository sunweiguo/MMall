package com.njupt.swg.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 【swg】.
 * @Date 2018/1/11 12:34
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Data
public class ProductDetailVo {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String subImages;
    private String detail;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String createTime;
    private String updateTime;

    private String imageHost;
    private Integer parentCategoryId;
}
