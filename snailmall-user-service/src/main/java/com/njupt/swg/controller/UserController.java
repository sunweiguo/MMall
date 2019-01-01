package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.cache.Parameters;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.CookieUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.IUserService;
import com.njupt.swg.vo.UserResVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author swg.
 * @Date 2019/1/1 13:12
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RequestMapping("user")
@RestController
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private CommonCacheUtil commonCacheUtil;

    /**
     * 用户登陆：验证参数、登陆、写到cookie中并且写到redis中
     * 用户登陆以后，点击其他需要登陆才能看的页面时，先判断是否前端是否有这个key，没有则提示需要登陆
     */
    @PostMapping("/login.do")
    public ServerResponse<UserResVO> login(HttpSession session, HttpServletResponse response, String username, String password){
        log.info("【用户{}开始登陆】",username);
        ServerResponse<UserResVO> userVOServerResponse = userService.login(username,password);
        if(userVOServerResponse.isSuccess()){
            //登陆成功，那么需要在redis中存储，并且将代表用户的sessionId写到前端浏览器的cookie中
            log.info("【用户{}cookie开始写入】",username);
            CookieUtil.writeLoginToken(response,session.getId());
            //写到redis中，将用户信息序列化，设置过期时间为30分钟
            log.info("【用户{}redis开始写入】",username);
            commonCacheUtil.cacheNxExpire(session.getId(), JsonUtil.obj2String(userVOServerResponse.getData()), Constants.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        log.info("【用户{}登陆成功】",username);
        return userVOServerResponse;
    }


    /**
     * 用户注册，要判断用户名和邮箱是否重复，这里用了分布式锁来防止用户名和邮箱可能出现重复
     */
    @PostMapping("/register.do")
    public ServerResponse register(User user){
        log.info("【开始注册】");
        //TODO 这里模拟高并发的注册场景，防止用户名字注册重复，所以需要加上分布式锁
        ServerResponse response = userService.register(user);

        return response;
    }

    /**
     * 判断用户名和邮箱是否重复
     */
    @RequestMapping("/check_valid.do")
    public ServerResponse checkValid(@RequestParam("str") String str,
                                     @RequestParam("type") String type){
        ServerResponse response = userService.checkValid(str,type);
        return response;
    }



}
