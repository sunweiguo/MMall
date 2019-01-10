package com.njupt.swg.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author swg.
 * @Date 2019/1/1 11:50
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Data
public class UserResVO {
    private int id;
    private String username;
    private String email;
    private int role;
    private String phone;
    private String question;
    private String answer;
    private Date createTime;//返回前端的是时间戳
    private Date updateTime;
}
