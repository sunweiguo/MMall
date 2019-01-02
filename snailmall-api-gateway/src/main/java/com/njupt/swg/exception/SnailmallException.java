package com.njupt.swg.exception;

import com.njupt.swg.resp.ResponseEnum;
import lombok.Getter;

/**
 * @Author swg.
 * @Date 2019/1/1 13:18
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Getter
public class SnailmallException extends RuntimeException{
    private int exceptionStatus = ResponseEnum.ERROR.getCode();

    public SnailmallException(String msg){
        super(msg);
    }

    public SnailmallException(int code,String msg){
        super(msg);
        exceptionStatus = code;
    }

}
