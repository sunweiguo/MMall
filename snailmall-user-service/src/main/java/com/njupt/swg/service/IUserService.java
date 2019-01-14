package com.njupt.swg.service;

import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.entity.User;
import com.njupt.swg.vo.UserResVO;

/**
 * @Author swg.
 * @Date 2018/12/31 21:07
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public interface IUserService {
    /** 用户登陆 **/
    ServerResponse<UserResVO> login(String username,String password);

    /** 用户注册 **/
    ServerResponse register(User user);

    /** 判断用户名和邮箱是否重复 **/
    ServerResponse checkValid(String str, String type);

    /** 根据用户名去获取设置的忘记密码的问题 **/
    ServerResponse getQuestionByUsername(String username);

    /** 校验问题对应的答案是否正确 **/
    ServerResponse checkAnswer(String username, String question, String answer);

    /** 重置密码 **/
    ServerResponse forgetResetPasswd(String username, String passwordNew, String forgetToken);

    /** 登陆状态下重置密码 **/
    ServerResponse resetPasswd(String passwordOld, String passwordNew, int userId);

    /** 登陆状态下更新个人信息（更新策略为：如果用户某一项不填，表示保持原样不变） **/
    ServerResponse updateInfomation(String email, String phone, String question, String answer, Integer userId);

    UserResVO getUserInfoFromDB(Integer userId);
}
