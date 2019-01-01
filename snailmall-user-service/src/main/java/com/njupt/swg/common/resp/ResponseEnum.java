package com.njupt.swg.common.resp;

import lombok.Getter;

/**
 * @Author swg.
 * @Date 2018/12/31 20:15
 * @CONTACT 317758022@qq.com
 * @DESC 基本的返回状态描述
 */
@Getter
public enum ResponseEnum {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    ILLEGAL_ARGUMENTS(2,"ILLEGAL_ARGUMENTS"),
    NEED_LOGIN(10,"NEED_LOGIN");

    private int code;
    private String desc;

    ResponseEnum(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
}
