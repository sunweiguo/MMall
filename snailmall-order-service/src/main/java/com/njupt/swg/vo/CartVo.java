package com.njupt.swg.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author swg.
 * @Date 2019/1/5 15:18
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Data
public class CartVo {
    private List<CartProductVo> cartProductVoList;
    private BigDecimal cartTotalPrice;
    private Boolean allChecked;//是否已经都勾选
    private String imageHost;
}
