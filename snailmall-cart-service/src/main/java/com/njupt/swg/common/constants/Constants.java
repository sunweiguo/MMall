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

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    /** 产品的状态 **/
    public interface Product{
        int PRODUCT_ON = 1;
        int PRODUCT_OFF = 2;
        int PRODUCT_DELETED = 3;
    }

    /***redis product stock**/
    public static final String PRODUCT_TOKEN_STOCK_PREFIX = "product__stock_";

    public static final String PRODUCT_TOKEN_PREFIX = "product__";


}
