package com.njupt.swg.controller;

import com.njupt.swg.resp.ResponseEnum;
import com.njupt.swg.resp.ServerResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/1/2 21:21
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
public class ErrorHandleController implements ErrorController {
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public ServerResponse error() {
        return ServerResponse.createByErrorCodeMessage(ResponseEnum.NEED_LOGIN.getCode(),"用户未登陆或者权限不足");
    }

}
