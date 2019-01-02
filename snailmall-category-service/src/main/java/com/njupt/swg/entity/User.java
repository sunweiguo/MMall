package com.njupt.swg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author swg.
 * @Date 2018/12/31 21:01
 * @CONTACT 317758022@qq.com
 * @DESC 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable {
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String question;

    private String answer;

    //角色0-管理员,1-普通用户
    private Integer role;

    private Date createTime;

    private Date updateTime;

}