package com.njupt.swg.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVLET_DETECTION_FILTER_ORDER;

/**
 * @Author swg.
 * @Date 2019/1/3 11:21
 * @CONTACT 317758022@qq.com
 * @DESC 令牌桶法限流 具体测试没做
 */
@Component
public class RateLimitFilter extends ZuulFilter {
    //放100个令牌
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(100);


    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SERVLET_DETECTION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        if(!RATE_LIMITER.tryAcquire()){
            //没有取到一个令牌的话，可以这样返回信息给前端
            context.set("状态码",401);
            context.set("error.message","用户没有获取到令牌");
        }
        return null;
    }
}
