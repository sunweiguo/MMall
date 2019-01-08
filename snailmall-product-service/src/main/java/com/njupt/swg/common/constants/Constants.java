package com.njupt.swg.common.constants;

import com.google.common.collect.Sets;
import java.util.Set;

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


    /** 产品的状态 **/
    public interface Product{
        int PRODUCT_ON = 1;
        int PRODUCT_OFF = 2;
        int PRODUCT_DELETED = 3;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }


    /***redis product**/
    public static final String PRODUCT_TOKEN_PREFIX = "product__";
    public static final int PRODUCT_EXPIRE_TIME = 60 * 60 * 24 * 300;

    public static final String PRODUCT_TOKEN_STOCK_PREFIX = "product__stock_";


}
