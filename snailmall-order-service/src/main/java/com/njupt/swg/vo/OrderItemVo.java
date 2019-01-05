package com.njupt.swg.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author swg.
 * @Date 2019/1/5 21:47
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Data
public class OrderItemVo {

    private Long orderNo;

    private Integer productId;

    private String productName;

    private String productImage;

    private BigDecimal currentUnitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String createTime;

}
