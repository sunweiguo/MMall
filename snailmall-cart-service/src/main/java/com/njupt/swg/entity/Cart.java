package com.njupt.swg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    private Integer checked;

    private Date createTime;

    private Date updateTime;
}