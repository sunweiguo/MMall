package com.njupt.swg.vo;

import lombok.Data;

/**
 * @Author swg.
 * @Date 2019/1/7 16:17
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Data
public class MessageVo {
    private Integer userId;
    private Integer shippingId;
    private CartVo cartVo;
    private long orderNo;
}
