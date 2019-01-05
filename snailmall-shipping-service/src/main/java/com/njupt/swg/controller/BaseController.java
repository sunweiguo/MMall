package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ResponseEnum;
import com.njupt.swg.common.utils.CookieUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author swg.
 * @Date 2019/1/5 16:19
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Slf4j
public class BaseController {

    @Autowired
    private CommonCacheUtil commonCacheUtil;

    User getCurrentUser(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isBlank(loginToken)){
            throw new SnailmallException("用户未登陆，无法获取当前用户信息");
        }
        String userJsonStr = commonCacheUtil.getCacheValue(loginToken);
        if(userJsonStr == null){
            throw new SnailmallException(ResponseEnum.NEED_LOGIN.getCode(),ResponseEnum.NEED_LOGIN.getDesc());
        }
        User user = JsonUtil.Str2Obj(userJsonStr,User.class);
        return user;
    }
}
