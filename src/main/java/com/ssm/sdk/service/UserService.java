package com.ssm.sdk.service;

import com.ssm.sdk.common.pojo.*;
import com.ssm.sdk.exception.SdkException;

import java.util.Map;

public interface UserService {
    Integer getUserIdByNum(String account);

    boolean doSendVerCode(String account) throws SdkException;

    UserTO doRegister(String account, String password, Integer type, Integer prtchid) throws SdkException;

    VerCodeTO getCodeByMobile(String account);

    void deleteCodeByMobile(String account);

    UserTO doBindAccount(String account, String password, Integer uid) throws SdkException;

    Integer getUserIdByName(String name);

    ResponseTO doLogin(String account, String password,Integer prtchid, Integer cpid, Integer seqnum, String ip, String ua) throws SdkException;

    ResponseTO updateUserPwd(String account, String password, String code) throws SdkException;

    ResponseTO bindNum(String account, String num, String code) throws SdkException;

    void doLoginServer(LoginServerLogTO log) throws SdkException;

    Integer getServerIdByGameAndSeq(Map<String, Integer> param);

    void addUserRole(UserRoleTO userRoleTO);

    void updateUserRole(UserRoleTO userRoleTO);

    UserRoleTO selectUserRole(UserRoleTO userRoleTO);


    OnlineUserTO getOnlineUser(String decodeTicket);

    void updateOnlineUser(OnlineUserTO onlineUser);

    void updateLogoutDate(String ticket);

    void updateUserDynamic(UserDynamicTO userDynamicTO);

    void updateUserDynamicServerId(UserDynamicTO userDynamicTO);
}