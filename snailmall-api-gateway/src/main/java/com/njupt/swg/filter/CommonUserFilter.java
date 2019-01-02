package com.njupt.swg.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.Parameters;
import com.njupt.swg.constants.Constants;
import com.njupt.swg.exception.SnailmallException;
import com.njupt.swg.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @Author swg.
 * @Date 2019/1/2 20:41
 * @CONTACT 317758022@qq.com
 * @DESC 普通用户，直接从cookie中读取，从redis中拿出来即可
 */
@Component
@Slf4j
public class CommonUserFilter extends ZuulFilter {

    @Autowired
    private CommonCacheUtil commonCacheUtil;
    @Autowired
    private Parameters parameters;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    //TODO 这里对路径拦截还需要再处理一下
    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //获取当前请求的url
        String url = request.getRequestURI();
        log.info("【当前访问的url为{}】",url);
        //从配置文件获取所有不需要校验的路径
        String[] passUrls = parameters.getNoneSecurityPortalPaths().toArray(new String[parameters.getNoneSecurityPortalPaths().size()]);
        for(String str:passUrls){
            if (url.contains(str) && !url.contains("manage")){
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        //1.读取cookie
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(Constants.RESP_STATUS_NOAUTH);
            throw new SnailmallException("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            throw new SnailmallException("用户未登录,无法获取当前用户信息");
        }
        return null;
    }
}
