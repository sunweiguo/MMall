package com.njupt.swg.service;

import com.njupt.swg.common.constants.Constants;
import com.njupt.swg.common.exception.SnailmallException;
import com.njupt.swg.common.resp.ServerResponse;
import com.njupt.swg.common.utils.MD5Util;
import com.njupt.swg.dao.UserMapper;
import com.njupt.swg.entity.User;
import com.njupt.swg.vo.UserResVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author swg.
 * @Date 2018/12/31 21:08
 * @CONTACT 317758022@qq.com
 * @DESC 登陆错误 code=1,msg=xxx 登陆成功 code=0,data=UserRespVO
 */
@Service
@Slf4j
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CuratorFramework zkClient;

    @Override
    public ServerResponse<UserResVO> login(String username,String password) {
        //1.校验参数不能为空
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            throw new SnailmallException("温馨提示：用户名或密码不能为空");
        }
        //2.根据用户名去取用户信息（本系统用户名不能重复）
        int resultCount = userMapper.selectByUsername(username);
        if(resultCount == 0){
            throw new SnailmallException("温馨提示：用户名不存在");
        }
        //3.走到这一步，说明存在该用户，下面就执行登陆校验
        String md5Passwd = MD5Util.MD5EncodeUtf8(password);
        User resultUser = userMapper.selectByUsernameAndPasswd(username,md5Passwd);
        if (resultUser == null){
            throw new SnailmallException("温馨提示：用户名或者密码不正确，请重试");
        }
        //4.走到这一步，说明用户名密码正确，应该返回成功
        UserResVO userResVO = new UserResVO();
        userResVO.setId(resultUser.getId());
        userResVO.setUsername(resultUser.getUsername());
        userResVO.setEmail(resultUser.getEmail());
        userResVO.setPhone(resultUser.getPhone());
        userResVO.setRole(resultUser.getRole());
        userResVO.setCreateTime(resultUser.getCreateTime());
        userResVO.setUpdateTime(new Date());

        return ServerResponse.createBySuccess(userResVO);
    }

    @Override
    public ServerResponse register(User user) {
        //1.校验参数是否为空
        if(StringUtils.isBlank(user.getUsername()) ||
                StringUtils.isBlank(user.getEmail()) ||
                StringUtils.isBlank(user.getPassword()) ||
                StringUtils.isBlank(user.getQuestion()) ||
                StringUtils.isBlank(user.getAnswer())){
            throw new SnailmallException("参数不能为空，请仔细填写");
        }
        //---开启锁
        InterProcessLock lock = null;
        try {
            lock = new InterProcessMutex(zkClient, Constants.USER_REGISTER_DISTRIBUTE_LOCK_PATH);
            boolean retry = true;
            do {
                if (lock.acquire(3000, TimeUnit.MILLISECONDS)){
                    log.info(user.getEmail()+Thread.currentThread().getName()+"获取锁");
                    //2.参数没问题的话，就校验一下名字是否已经存在
                    ServerResponse response = this.checkValid(user.getUsername(),Constants.USERNAME);
                    if(!response.isSuccess()){
                        //说明用户名已经重复了
                        return response;
                    }
                    //3.再校验一下邮箱是否存在
                    response = this.checkValid(user.getEmail(),Constants.EMAIL);
                    if(!response.isSuccess()){
                        //说明用户名已经重复了
                        return response;
                    }
                    //4.重复校验通过之后就可以塞入这条数据了
                    user.setRole(Constants.Role.ROLE_CUSTOME);//普通用户
                    user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
                    userMapper.insert(user);
                    //跳出循环
                    retry = false;
                }
                log.info("【获取锁失败，继续尝试...】");
                //可以适当休息一会
            }while (retry);
        }catch (Exception e){
            log.error("【校验用户所填的用户名或者密码出现问题】",e);
            throw new SnailmallException("分布式锁校验出错");
        }finally {
            //---释放锁
            if(lock != null){
                try {
                    lock.release();
                    log.info(user.getEmail()+Thread.currentThread().getName()+"释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse checkValid(String str, String type) {
        //校验参数是否为空
        if(StringUtils.isBlank(str) || StringUtils.isBlank(type)){
            throw new SnailmallException("参数有问题");
        }
        if(Constants.USERNAME.equalsIgnoreCase(type)){
            //如果是username类型，那么就根据str为username去数据库查询
            int resultCount = userMapper.selectByUsername(str);
            if(resultCount > 0){
                //说明数据库已经存在这个username的用户了，返回用户已存在
                return ServerResponse.createByErrorMessage("用户已经存在");
            }
        }else if(Constants.EMAIL.equals(type)){
            //如果是email类型，就根据str为email去数据库查询
            int resultCount = userMapper.selectByEmail(str);
            if(resultCount > 0){
                //说明数据库已经存在这个email的用户了，返回用户已存在
                return ServerResponse.createByErrorMessage("邮箱已经存在");
            }
        }
        return ServerResponse.createBySuccess("校验成功，用户名和邮箱都合法");
    }


}
