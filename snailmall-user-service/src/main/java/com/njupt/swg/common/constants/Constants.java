package com.njupt.swg.common.constants;

/**
 * @Author swg.
 * @Date 2019/1/1 13:19
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public class Constants {
    /**自定义状态码 start**/
    public static final int RESP_STATUS_OK = 200;

    public static final int RESP_STATUS_NOAUTH = 401;

    public static final int RESP_STATUS_INTERNAL_ERROR = 500;

    public static final int RESP_STATUS_BADREQUEST = 400;

    /**自定义状态码 end**/

    /***redis user相关的key以这个打头**/
    public static final String TOKEN_PREFIX = "user_";

    /**
     * 用户登陆redis的过期时间
     */
    public interface RedisCacheExtime{
        int REDIS_SESSION_EXTIME = 60 * 60 * 10;//30分钟
    }

    /** 用户注册判断重复的参数类型 start **/
    public static final String EMAIL = "email";

    public static final String USERNAME = "username";
    /** 用户注册判断重复的参数类型 end **/

    /** 用户角色 **/
    public interface Role{
        int ROLE_CUSTOME = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员用户
    }

    /**用户注册分布式锁路径***/
    public static final String USER_REGISTER_DISTRIBUTE_LOCK_PATH = "/user_reg";

}
