package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.CookieUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.IUserService;
import com.njupt.swg.vo.UserResVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    //TODO 登陆接口暂时开放GET请求，便于测试
    @GetMapping("/login.do")
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
        //这里模拟高并发的注册场景，防止用户名字注册重复，所以需要加上分布式锁
        ServerResponse response = userService.register(user);

        return response;
    }

    /**
     * 判断用户名和邮箱是否重复
     */
    @GetMapping("/check_valid.do")
    public ServerResponse checkValid(@RequestParam("str") String str,
                                     @RequestParam("type") String type){
        ServerResponse response = userService.checkValid(str,type);
        return response;
    }

    /**
     * 获取登陆状态用户信息
     * 本地测试的时候，由于cookie是写到oursnai.cn域名下面的，所以需要在hosts文件中添加127.0.0.1 oursnail.cn这个解析
     * 在浏览器中测试的时候，将login方法暂时开放为GET请求，然后请求路径为：http://oursnail.cn:8081/user/login.do?username=admin&password=123456
     * 同样地，在测试获取登陆用户信息接口，也要按照域名来请求，否则拿不到token：http://oursnail.cn:8081/user/get_user_info.do
     */
    @GetMapping("/get_user_info.do")
    public ServerResponse getUserInfo(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);
        UserResVO userResVO = new UserResVO();
        userResVO.setId(currentUser.getId());
        userResVO.setUsername(currentUser.getUsername());
        userResVO.setEmail(currentUser.getEmail());
        userResVO.setPhone(currentUser.getPhone());
        userResVO.setRole(currentUser.getRole());
        userResVO.setCreateTime(currentUser.getCreateTime());
        userResVO.setUpdateTime(currentUser.getUpdateTime());
        return ServerResponse.createBySuccess("登陆用户获取自身信息成功",userResVO);
    }


    /**
     * 根据用户名去拿到对应的问题
     */
    @PostMapping("/forget_get_question.do")
    public ServerResponse forgetGetQuestion(String username){
        ServerResponse response = userService.getQuestionByUsername(username);
        return response;
    }

    /**
     * 校验答案是否正确
     */
    @PostMapping("/forget_check_answer.do")
    public ServerResponse forgetCheckAnswer(String username,String question,String answer){
        ServerResponse response = userService.checkAnswer(username,question,answer);
        return response;
    }


    /**
     * 重置密码
     */
    @PostMapping("/forget_reset_password.do")
    public ServerResponse forgetResetPasswd(String username,String passwordNew,String forgetToken){
        ServerResponse response = userService.ResetPasswd(username,passwordNew,forgetToken);
        return response;
    }



}
