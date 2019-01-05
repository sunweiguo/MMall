package com.njupt.swg.common.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author swg.
 * @Date 2018/12/31 20:11
 * @CONTACT 317758022@qq.com
 * @DESC 作为本项目的通用的返回封装类
 */
@Getter
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    public ServerResponse(){}

    private ServerResponse(int status){
        this.status = status;
    }
    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }
    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }
    private ServerResponse(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.status == ResponseEnum.SUCCESS.getCode();
    }

    /**
     * 成功的方法
     */
    public static <T>ServerResponse<T> createBySuccess(){
        return new ServerResponse<>(ResponseEnum.SUCCESS.getCode(),ResponseEnum.SUCCESS.getDesc());
    }
    public static <T>ServerResponse<T> createBySuccessMessage(String message){
        return new ServerResponse<>(ResponseEnum.SUCCESS.getCode(),message);
    }
    public static <T>ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<>(ResponseEnum.SUCCESS.getCode(),data);
    }
    public static <T>ServerResponse<T> createBySuccess(String message,T data){
        return new ServerResponse<>(ResponseEnum.SUCCESS.getCode(),message,data);
    }

    /**
     * 失败的方法
     */
    public static <T>ServerResponse<T> createByError(){
        return new ServerResponse<>(ResponseEnum.ERROR.getCode(),ResponseEnum.ERROR.getDesc());
    }
    public static <T>ServerResponse<T> createByErrorMessage(String msg){
        return new ServerResponse<>(ResponseEnum.ERROR.getCode(),msg);
    }
    public static <T>ServerResponse<T> createByErrorCodeMessage(int code,String msg){
        return new ServerResponse<>(code,msg);
    }



}
