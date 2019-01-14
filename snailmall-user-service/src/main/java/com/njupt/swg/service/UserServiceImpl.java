package com.njupt.swg.service;

import com.njupt.swg.cache.CommonCacheUtil;
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
import java.util.UUID;
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
    @Autowired
    private CommonCacheUtil commonCacheUtil;

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
        userResVO.setQuestion(resultUser.getQuestion());
        userResVO.setAnswer(resultUser.getAnswer());
        userResVO.setCreateTime(resultUser.getCreateTime());
        userResVO.setUpdateTime(new Date());

        return ServerResponse.createBySuccess("用户登陆成功",userResVO);
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

    @Override
    public ServerResponse getQuestionByUsername(String username) {
        //1.校验参数
        if(StringUtils.isBlank(username)){
            return ServerResponse.createByErrorMessage("用户名不能为空");
        }
        //2.根据username去获取题目
        User user = userMapper.getUserByUsername(username);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = user.getQuestion();
        if(StringUtils.isBlank(question)){
            return ServerResponse.createByErrorMessage("该用户没有设置对应的问题");
        }
        return ServerResponse.createBySuccess(question);
    }

    @Override
    public ServerResponse checkAnswer(String username, String question, String answer) {
        //1.校验参数是否正确
        if(StringUtils.isBlank(username) || StringUtils.isBlank(question) || StringUtils.isBlank(answer)){
            return ServerResponse.createByErrorMessage("参数有问题");
        }
        //2.参数没有问题之后，就可以去校验答案是否正确了
        User user = userMapper.getUserByUsernameQuestionAnswer(username,question,answer);
        if(user != null){
            //首先根据规则key去redis取，如果还有没有过期的key，就可以直接拿来用了，不用再重新生成
            String forgetToken = commonCacheUtil.getCacheValue(Constants.TOKEN_PREFIX+username);
            if(StringUtils.isNotBlank(forgetToken)){
                return ServerResponse.createBySuccess(forgetToken);
            }
            //取不到值，并且答案是对的，那么就重新生成一下吧！
            forgetToken = UUID.randomUUID().toString();
            commonCacheUtil.cacheNxExpire(Constants.TOKEN_PREFIX+username,forgetToken,60*60*12);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案有误");
    }

    @Override
    public ServerResponse forgetResetPasswd(String username, String passwordNew, String forgetToken) {
        //1.校验参数
        if(StringUtils.isBlank(username) || StringUtils.isBlank(passwordNew) || StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数有误，修改密码操作失败");
        }
        //2.根据username去获取用户
        User user = userMapper.getUserByUsername(username);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户名不存在，修改密码操作失败");
        }
        //3.从redis中获取token，看是否超时
        String redisToken = commonCacheUtil.getCacheValue(Constants.TOKEN_PREFIX+username);
        if(redisToken == null){
            return ServerResponse.createByErrorMessage("token已经过期，修改密码操作失败");
        }
        //4.看前端传过来的token与redis中取出来的token是否相等
        if(!StringUtils.equals(redisToken,forgetToken)){
            return ServerResponse.createByErrorMessage("token错误，修改密码操作失败");
        }
        //5.判断密码是否重复
        String MD5Passwd = MD5Util.MD5EncodeUtf8(passwordNew);
        if(user.getPassword().equals(MD5Passwd)){
            return ServerResponse.createByErrorMessage("不要使用重复密码！");
        }
        //6.重置密码
        user.setPassword(MD5Passwd);
        int result = userMapper.updateByPrimaryKeySelective(user);
        if(result > 0){
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse resetPasswd(String passwordOld, String passwordNew, int userId) {
        //1.校验参数
        if(StringUtils.isBlank(passwordOld) || StringUtils.isBlank(passwordNew)){
            return ServerResponse.createByErrorMessage("参数有误");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("无用户登陆");
        }
        //2.校验老的密码
        String passwordOldMD5 = MD5Util.MD5EncodeUtf8(passwordOld);
        if(!StringUtils.equals(passwordOldMD5,user.getPassword())){
            return ServerResponse.createByErrorMessage("老密码输错啦...");
        }
        //3.重置新的密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("更新密码成功");
        }
        return ServerResponse.createByErrorMessage("更新密码失败");
    }

    @Override
    public ServerResponse updateInfomation(String email, String phone, String question, String answer, Integer userId) {
        //1.获取当前登陆用户
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("获取当前登陆用户失败，请重新登陆");
        }

        //2.校验参数
        if(StringUtils.isBlank(email) || StringUtils.isBlank(phone) || StringUtils.isBlank(question) || StringUtils.isBlank(answer)){
            return ServerResponse.createByErrorMessage("更新的数据不能存在空值!");
        }

        //2.修改用户信息应该并发不大，所以不用加锁了，这里校验邮箱是否重复
        Integer queryCount = userMapper.checkEmailValid(email,userId);
        if(queryCount > 0){
            //说明这个邮箱已经被其他用户占用了，所以不能使用
            return ServerResponse.createByErrorMessage("此邮箱已经被占用，换个试试~");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(email);
        updateUser.setPhone(phone);
        updateUser.setQuestion(question);
        updateUser.setAnswer(answer);

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);

        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("更新信息成功");
        }

        return ServerResponse.createByErrorMessage("更新用户信息失败");
    }

    @Override
    public UserResVO getUserInfoFromDB(Integer userId) {
        UserResVO userResVO = new UserResVO();
        User userDB = userMapper.selectByPrimaryKey(userId);
        if(userDB != null){
            userResVO.setId(userId);
            userResVO.setUsername(userDB.getUsername());
            userResVO.setEmail(userDB.getEmail());
            userResVO.setRole(userDB.getRole());
            userResVO.setPhone(userDB.getPhone());
            userResVO.setQuestion(userDB.getQuestion());
            userResVO.setAnswer(userDB.getAnswer());
            userResVO.setCreateTime(userDB.getCreateTime());
            userResVO.setUpdateTime(userDB.getUpdateTime());
        }
        return userResVO;
    }


}
