package com.ssm.sdk.dao;

import com.ssm.sdk.common.pojo.LoginServerLogTO;
import com.ssm.sdk.common.pojo.UserDynamicTO;
import com.ssm.sdk.common.pojo.UserRoleTO;
import com.ssm.sdk.common.pojo.UserTO;

import java.util.Map;

public interface UserDao {
    Integer getUserIdByNum(String num);

    Integer getUserIdByName(String num);

    void addUser(UserTO user);

    void addUserName(UserTO user);

    void addUserMobile(UserTO user);

    void addUserDynamic(Integer id);

    void updateNameAndPassword(UserTO user);

    void updateNumAndPassword(UserTO user);

    void updateName(Map param);

    UserTO getUserById(Integer userId);

    Integer getRegistChannel(Map<String, Object> param);

    void addGameRegistLog(Map<String, Object> param);

    void updatePwdByAccountNum(UserTO userTO);

    void updateNumByAccount(UserTO userTO);

    void addLoginServerLog(LoginServerLogTO log);

    void addUserRole(UserRoleTO userRoleTO);

    void updateUserRole(UserRoleTO userRoleTO);

    UserRoleTO selectUserRole(UserRoleTO userRoleTO);

    void updateUserDynamic(UserDynamicTO userDynamicTO);

    void updateUserDynamicServerId(UserDynamicTO userDynamicTO);
}
