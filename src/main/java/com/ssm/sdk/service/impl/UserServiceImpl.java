package com.ssm.sdk.service.impl;

import com.ssm.sdk.cache.GameCache;
import com.ssm.sdk.common.pojo.*;
import com.ssm.sdk.common.util.DigestUtils;
import com.ssm.sdk.common.util.ErrorConstant;
import com.ssm.sdk.dao.GameDao;
import com.ssm.sdk.dao.OnlineUserDao;
import com.ssm.sdk.dao.UserDao;
import com.ssm.sdk.dao.VerCodeDao;
import com.ssm.sdk.exception.SdkException;
import com.ssm.sdk.service.UserService;
import com.ssm.sdk.util.ResponseTOUtil;
import com.ssm.sdk.util.SdkConstants;
import com.ssm.sdk.util.SendMsgUtil;
import com.ssm.sdk.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private VerCodeDao verCodeDao;
    @Autowired
    private OnlineUserDao onlineUserDao;
    @Autowired
    private GameDao gameDao;

    @Override
    public Integer getUserIdByNum(String account) {
        return userDao.getUserIdByNum(account);
    }

    @Override
    public boolean doSendVerCode(String account) throws SdkException {
        //先清空已发送过的短信
        verCodeDao.deleteCodeByMobile(account);
        String randomCode = SendMsgUtil.getRandomCode(4);
        VerCodeTO code = new VerCodeTO();

        //如果不是第一次登陆，即为修改密码所发验证码，此时需添加userId
        Integer userId = userDao.getUserIdByNum(account);
        if (userId != null) {
            code.setUserId(userId);
        }

        code.setMobile(account);
        code.setCode(randomCode);
        verCodeDao.addCode(code);
        boolean success = SendMsgUtil.send(code);
        if (!success) {
            throw new SdkException(ErrorConstant.DATABASE_ERROR);
        }
        return success;
    }

    @Override
    public Integer getUserIdByName(String name) {
        return userDao.getUserIdByName(name);
    }

    @Override
    public ResponseTO doLogin(String account, String password, Integer prtchid, Integer cpid, Integer seqnum, String ip, String ua) throws SdkException {
        GameTO game = GameCache.getGameByCpAndSeq(cpid, seqnum);
        if (game == null) {
            throw new SdkException(ErrorConstant.GAME_NOT_EXIST);
        }
        //判断为手机号登陆还是用户名登陆，并获取相应的userId
        Integer userId = null;
        if (UserUtil.checkMobile(account)) {
            userId = userDao.getUserIdByNum(account);
        } else if (UserUtil.chackUserName(account)) {
            userId = userDao.getUserIdByName(account);
        } else {
            throw new SdkException(ErrorConstant.INVALID_ACCOUNT_INPUT);
        }
        if (userId == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        //校验并获取密码
        password = UserUtil.getPwd(password, cpid);
        //通过userId获取user，并比较密码是否相同
        UserTO user = userDao.getUserById(userId);
        if (!user.getPassword().equals(DigestUtils.getMD5(password + SdkConstants.PASSWORD_SALT))) {
            throw new SdkException(ErrorConstant.ACCOUNT_PASSWORD_ERROR);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("gameId", game.getId());
        Integer channel = userDao.getRegistChannel(param);
        if (channel == null) {
            param.put("channelId", prtchid);
            userDao.addGameRegistLog(param);
        }
        OnlineUserTO onlineUser = new OnlineUserTO();
        onlineUser.setGameId(game.getId());
        onlineUser.setUserId(userId);
        onlineUserDao.deleteOnlineUser(onlineUser);//踢掉其他的登陆信息
        onlineUser.setLoginAccount(account);
        onlineUser.setIp(ip);
        onlineUser.setUa(ua);
        String ticket = DigestUtils.getMD5(userId + System.currentTimeMillis() + SdkConstants.PASSWORD_SALT);
        onlineUser.setTicket(ticket);
        onlineUserDao.addOnlineUser(onlineUser);

        UserDynamicTO userDynamicTO = new UserDynamicTO();
        userDynamicTO.setUserId(userId);
        userDynamicTO.setLastIp(ip);
        userDynamicTO.setLastUa(ua);
        userDao.updateUserDynamic(userDynamicTO);

        user.setPassword(null);//返回用户，不写密码
        return ResponseTOUtil.getSuccessResult(user, ticket);
    }

    @Override
    public ResponseTO updateUserPwd(String account, String password, String code) throws SdkException {
        if (StringUtils.isEmpty(account) || !UserUtil.checkMobile(account)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(password)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (!UserUtil.checkPassword(password)) {
            throw new SdkException((ErrorConstant.INVALID_PASSWORD_INPUT));
        }
        VerCodeTO codeTO = verCodeDao.getCodeByMobile(account);
        Integer userId = userDao.getUserIdByNum(account);
        if (userId == null || !userId.equals(codeTO.getUserId())) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        if (!code.equals(codeTO.getCode())) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        UserTO userTO = new UserTO();
        userTO.setId(userId);
        userTO.setNum(account);
        userTO.setPassword(DigestUtils.getMD5(password + SdkConstants.PASSWORD_SALT));
        userDao.updatePwdByAccountNum(userTO);
        // 验证成功后删除验证码
        deleteCodeByMobile(account);
        return ResponseTOUtil.getSuccessResult(userTO, null);
    }

    @Override
    public ResponseTO bindNum(String account, String num, String code) throws SdkException {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(num) || StringUtils.isEmpty(code)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (!UserUtil.checkMobile(num)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (!UserUtil.chackUserName(account)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        Integer userId = getUserIdByName(account);
        UserTO userTO = userDao.getUserById(userId);
        if (getUserIdByNum(num) != null) {
            throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
        }
        VerCodeTO codeTO = verCodeDao.getCodeByMobile(num);
        if (codeTO == null || !code.equals(codeTO.getCode())) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        userTO.setNum(num);
        userDao.updateNumByAccount(userTO);
        userDao.addUserMobile(userTO);

        // 验证成功后删除验证码
        deleteCodeByMobile(account);
        return ResponseTOUtil.getSuccessResult(userTO, null);
    }

    @Override
    public void doLoginServer(LoginServerLogTO log) throws SdkException {
        userDao.addLoginServerLog(log);
    }

    public Integer getServerIdByGameAndSeq(Map<String, Integer> param) {
        return gameDao.getServerIdByGameAndSeq(param);
    }

    @Override
    public void addUserRole(UserRoleTO userRoleTO) {
        userDao.addUserRole(userRoleTO);
    }

    @Override
    public void updateUserRole(UserRoleTO userRoleTO) {
        userDao.updateUserRole(userRoleTO);
    }

    @Override
    public UserRoleTO selectUserRole(UserRoleTO userRoleTO) {
        return userDao.selectUserRole(userRoleTO);
    }

    @Override
    public OnlineUserTO getOnlineUser(String decodeTicket) {
        return onlineUserDao.getOnlineUser(decodeTicket);
    }

    @Override
    public void updateOnlineUser(OnlineUserTO onlineUser) {
        onlineUserDao.updateOnlineUser(onlineUser);
    }

    @Override
    public void updateLogoutDate(String ticket) {
        onlineUserDao.updateLogoutDate(ticket);
    }

    @Override
    public void updateUserDynamic(UserDynamicTO userDynamicTO) {
        userDao.updateUserDynamic(userDynamicTO);
    }

    @Override
    public void updateUserDynamicServerId(UserDynamicTO userDynamicTO) {
        userDao.updateUserDynamicServerId(userDynamicTO);
    }


    @Override
    public UserTO doRegister(String account, String password, Integer type, Integer channelId) throws SdkException {
        UserTO user = new UserTO();

        Integer accountType = null;
        if (UserUtil.checkMobile(account)) {
            //根据手机号注册
            if (getUserIdByNum(account) != null) {
                throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
            }
            user.setNum(account);
            accountType = 1;
        } else if (UserUtil.chackUserName(account)) {
            //根据用户名注册
            if (getUserIdByName(account) != null) {
                throw new SdkException(ErrorConstant.USERNAME_ALREADY_EXIST);
            }
            user.setName(account);
            accountType = 2;
        } else {
            throw new SdkException(ErrorConstant.INVALID_ACCOUNT_INPUT);
        }
        user.setChannelId(channelId);
        user.setUserType(type);
        user.setPassword(DigestUtils.getMD5(password + SdkConstants.PASSWORD_SALT));
        userDao.addUser(user);
        if (accountType == 1) {
            userDao.addUserMobile(user);
        } else {
            userDao.addUserName(user);
        }
        userDao.addUserDynamic(user.getId());
        return user;
    }

    @Override
    public VerCodeTO getCodeByMobile(String account) {
        return verCodeDao.getCodeByMobile(account);
    }

    @Override
    public void deleteCodeByMobile(String account) {
        verCodeDao.deleteCodeByMobile(account);
    }

    @Override
    public UserTO doBindAccount(String account, String password, Integer userId) throws SdkException {
        UserTO temp = userDao.getUserById(userId);
        if (temp == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        if (temp.getUserType() != SdkConstants.USER_TYPE_VISITOR) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_VISITOR);
        }
        UserTO user = new UserTO();
        user.setId(userId);
        user.setPassword(DigestUtils.getMD5(password + SdkConstants.PASSWORD_SALT));
        if (UserUtil.checkMobile(account)) {//绑定手机号
            if (getUserIdByNum(account) != null) {
                throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
            }
            user.setNum(account);
            userDao.updateNumAndPassword(user);
            userDao.addUserMobile(user);
        } else {//绑定用户名
            if (getUserIdByName(account) != null) {
                throw new SdkException(ErrorConstant.USERNAME_ALREADY_EXIST);
            }
            user.setName(account);
            userDao.updateNameAndPassword(user);
            Map<String, Object> param = new HashMap<>();
            param.put("name", account);
            param.put("userId", userId);
            userDao.updateName(param);
        }
        return user;
    }
}
