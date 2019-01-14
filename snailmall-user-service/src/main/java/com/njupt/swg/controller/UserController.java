package com.njupt.swg.controller;

import com.njupt.swg.cache.CommonCacheUtil;
import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.CookieUtil;
import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.entity.User;
import com.njupt.swg.service.IUserService;
import com.njupt.swg.vo.UserResVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
 * @DESC 用户模块，POSTMAN简单测试通过
 */

//TODO 先全部开放GET请求
@RequestMapping("user")
@RestController
@Slf4j
// 表示标识这个类是swagger的资源
@Api(value = "UserController", tags = {"用户服务接口"})
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private CommonCacheUtil commonCacheUtil;

    /**
     * 用户登陆：验证参数、登陆、写到cookie中并且写到redis中
     * 用户登陆以后，点击其他需要登陆才能看的页面时，先判断是否前端是否有这个key，没有则提示需要登陆
     */
    @ApiOperation(value="用户登陆", notes="输入用户名，密码，不能为空")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "用户密码", required = true, dataType = "String")
    })
    @RequestMapping("/login.do")
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
    @ApiOperation(value="创建用户", notes="根据User对象创建用户")
    @ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User")
    @RequestMapping("/register.do")
    public ServerResponse register(User user){
        log.info("【开始注册】");
        //这里模拟高并发的注册场景，防止用户名字注册重复，所以需要加上分布式锁
        ServerResponse response = userService.register(user);
        log.info("【用户注册成功】");
        return response;
    }

    /**
     * 判断用户名和邮箱是否重复
     */
    @ApiOperation(value="验证用户名和邮箱是否重复", notes="用户名和邮箱都不能用已经存在的")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "str", value = "输入参数", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "参数类型", required = true, dataType = "String")
    })
    @RequestMapping("/check_valid.do")
    public ServerResponse checkValid(@RequestParam("str") String str,
                                     @RequestParam("type") String type){
        log.info("【开始验证用户名和邮箱是否重复】");
        ServerResponse response = userService.checkValid(str,type);
        return response;
    }

    /**
     * 获取登陆状态用户信息
     * 本地测试的时候，由于cookie是写到oursnai.cn域名下面的，所以需要在hosts文件中添加127.0.0.1 oursnail.cn这个解析
     * 在浏览器中测试的时候，将login方法暂时开放为GET请求，然后请求路径为：http://oursnail.cn:8081/user/login.do?username=admin&password=123456
     * 同样地，在测试获取登陆用户信息接口，也要按照域名来请求，否则拿不到token：http://oursnail.cn:8081/user/get_user_info.do
     */
    @ApiOperation(value="获取用户个人信息", notes="登陆状态下获取")
    @RequestMapping("/get_user_info.do")
    public ServerResponse getUserInfo(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            log.info("【用户未登录,无法获取当前用户信息】");
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            log.info("【用户未登录,无法获取当前用户信息】");
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);
        UserResVO userResVO = userService.getUserInfoFromDB(currentUser.getId());
        return ServerResponse.createBySuccess("登陆用户获取自身信息成功",userResVO);
    }

    /**
     * 根据用户名去拿到对应的问题
     */
    @ApiOperation(value="根据用户名去拿到对应的问题", notes="忘记密码时首先根据用户名去获取设置的问题")
    @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String")
    @RequestMapping("/forget_get_question.do")
    public ServerResponse forgetGetQuestion(String username){
        log.info("【用户{}忘记密码，点击忘记密码输入用户名】",username);
        ServerResponse response = userService.getQuestionByUsername(username);
        return response;
    }

    /**
     * 校验答案是否正确
     */
    @ApiOperation(value="校验答案是否正确", notes="忘记密码时输入正确的用户名之后就可以获取到问题，此时就可以输入答案")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "question", value = "设置的问题", required = true, dataType = "String"),
            @ApiImplicitParam(name = "answer", value = "提交的答案", required = true, dataType = "String")
    })
    @RequestMapping("/forget_check_answer.do")
    public ServerResponse forgetCheckAnswer(String username,String question,String answer){
        log.info("【用户{}忘记密码，提交问题答案】",username);
        ServerResponse response = userService.checkAnswer(username,question,answer);
        return response;
    }


    /**
     * 忘记密码的重置密码
     */
    @ApiOperation(value="忘记密码的重置密码", notes="输入新的密码，要进行token的校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "passwordNew", value = "新密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "forgetToken", value = "前端保存的token", required = true, dataType = "String")
    })
    @RequestMapping("/forget_reset_password.do")
    public ServerResponse forgetResetPasswd(String username,String passwordNew,String forgetToken){
        log.info("【用户{}忘记密码，输入新密码】",username);
        ServerResponse response = userService.forgetResetPasswd(username,passwordNew,forgetToken);
        return response;
    }

    /**
     * 登陆状态的重置密码
     */
    @ApiOperation(value="登陆状态的重置密码", notes="登陆的时候只需要输入老的密码和新密码即可")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "passwordOld", value = "老密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "passwordNew", value = "新密码", required = true, dataType = "String")
    })
    @RequestMapping("/reset_password.do")
    public ServerResponse resetPasswd(String passwordOld,String passwordNew,HttpServletRequest request){
        //1.读取cookie
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);
        log.info("【用户{}重置密码】",currentUser);

        ServerResponse response = userService.resetPasswd(passwordOld,passwordNew,currentUser.getId());
        return response;
    }

    /**
     * 更新当前登陆用户信息
     */
    @ApiOperation(value="更新当前登陆用户信息", notes="更新用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phone", value = "电话", required = true, dataType = "String"),
            @ApiImplicitParam(name = "question", value = "问题", required = true, dataType = "String"),
            @ApiImplicitParam(name = "answer", value = "答案", required = true, dataType = "String")
    })
    @RequestMapping("/update_information.do")
    public ServerResponse updateInformation(String email,String phone,String question,String answer,HttpServletRequest request){
        //1.读取cookie
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        //2.从redis中获取用户信息
        String userStr = commonCacheUtil.getCacheValue(loginToken);
        if(userStr == null){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        User currentUser = JsonUtil.Str2Obj(userStr,User.class);

        ServerResponse response = userService.updateInfomation(email,phone,question,answer,currentUser.getId());
        return response;
    }

    /**
     * 登出,删除cookie和redis即可
     */
    @ApiOperation(value="登出", notes="退出登陆，删除cookie和redis缓存")
    @RequestMapping("/logout.do")
    public ServerResponse logout(HttpServletRequest request,HttpServletResponse response){
        log.info("【用户删除cookie】");
        //1.删除cookie
        String loginToken = CookieUtil.readLoginToken(request);
        CookieUtil.delLoginToken(request,response);
        log.info("【用户删除redis缓存】");
        //2.删除redis中缓存记录
        commonCacheUtil.delKey(loginToken);
        return ServerResponse.createBySuccess();
    }




}
