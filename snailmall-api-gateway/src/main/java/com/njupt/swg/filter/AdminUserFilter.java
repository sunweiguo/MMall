package com.njupt.swg.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.Parameters;
import com.njupt.swg.constants.Constants;
import com.njupt.swg.entity.User;
import com.njupt.swg.exception.SnailmallException;
import com.njupt.swg.resp.ResponseEnum;
import com.njupt.swg.resp.ServerResponse;
import com.njupt.swg.utils.CookieUtil;
import com.njupt.swg.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @Author swg.
 * @Date 2019/1/3 10:21
 * @CONTACT 317758022@qq.com
 * @DESC 关于后台管理系统，登陆不需要拦截，对于需要认证的富文本上传，需要特定的格式，所以，这里先对富文本放开，到controller里面进行特别处理
 * 前台，就不再在这里进行处理了，因为前台需要防止用户同级的攻击，所以需要userID,放到这里，controller那边不好处理
 */
@Slf4j
@Component
public class AdminUserFilter extends ZuulFilter {
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
        return PRE_DECORATION_FILTER_ORDER-1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //获取当前请求的url
        String url = request.getRequestURI();
        //前端的路径不在这里校验，直接放过
        if (!url.contains("manage")){
            log.info("【{}不需要进行权限校验】",url);
            return false;
        }
        if(url.contains("upload")){
            log.info("【{}不需要进行权限校验】",url);
            return false;
        }
        //从配置文件获取所有门户需要校验的路径
//        String[] passUrls = parameters.getNoneSecurityAdminPaths().toArray(new String[parameters.getNoneSecurityAdminPaths().size()]);
//        for(String str:passUrls){
//            //指定的路径比较特殊，也不在这里校验
//            if(url.contains("manage") && url.contains(str)){
//                log.info("【{}不需要进行权限校验】",url);
//                return false;
//            }
//        }
        log.info("【{}----需要进行权限校验，必须是管理员身份才可以进入】",url);
        return true;
    }

    @Override
    public ServerResponse run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //校验是否为管理员身份
        String loginToken = CookieUtil.readLoginToken(request);
        log.info("【获取cookie----{}】",loginToken);
        if(StringUtils.isEmpty(loginToken)){
//            // 过滤该请求，不对其进行路由
//            requestContext.setSendZuulResponse(false);
//            //返回错误代码
//            requestContext.setResponseStatusCode(Constants.RESP_STATUS_NOAUTH);
//            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
            this.returnMsg(requestContext);
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
            //throw new SnailmallException("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        log.info("【从redis中获取用户信息:{}】",userStr);
        if(userStr == null){
//            // 过滤该请求，不对其进行路由
//            requestContext.setSendZuulResponse(false);
//            //返回错误代码
//            requestContext.setResponseStatusCode(Constants.RESP_STATUS_NOAUTH);
//            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息"); //SnailmallException(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
            this.returnMsg(requestContext);
            return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
        }
        String url = request.getRequestURI();
        log.info("【获取当前url:{}】",url);
        if(url.contains("manage")){
            log.info("【来到了管理后台，需要校验权限】");
            User currentUser = JsonUtil.Str2Obj(userStr,User.class);
            log.info("【当前登陆的用户为:{}】",currentUser);
            if(!currentUser.getRole().equals(Constants.Role.ROLE_ADMIN)){
                //不是管理员报错
                log.error("【当前登陆用户不是管理员身份】");
                // 过滤该请求，不对其进行路由
//                requestContext.setSendZuulResponse(false);
//                //返回错误代码
//                requestContext.setResponseStatusCode(Constants.RESP_STATUS_NOAUTH);
//                return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
                this.returnMsg(requestContext);
                return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
                //throw new SnailmallException("用户权限不够");
            }
        }
        return null;
    }

    //返回没权限消息
    private void returnMsg(RequestContext ctx){
        ctx.getResponse().setContentType("application/json; charset=utf-8");
        ctx.setSendZuulResponse(false); //令zuul过滤该请求，不对其进行路由
        ctx.setResponseStatusCode(Constants.RESP_STATUS_OK);
        ctx.setResponseBody(JsonUtil.obj2String(ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息")));
    }
}
