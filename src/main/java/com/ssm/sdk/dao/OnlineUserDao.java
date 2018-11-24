package com.ssm.sdk.dao;

import com.ssm.sdk.common.pojo.OnlineUserTO;

public interface OnlineUserDao {
    void addOnlineUser(OnlineUserTO onlineUser);

    OnlineUserTO getOnlineUser(String decodeTicket);

//    修改gameServerId和LAST_ACT
    void updateOnlineUser(OnlineUserTO onlineUser);

    void deleteOnlineUser(OnlineUserTO onlineUser);

    void updateLogoutDate(String ticket);
}
