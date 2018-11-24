package com.ssm.sdk.controller;

import com.ssm.sdk.cache.GameCache;
import com.ssm.sdk.common.pojo.*;
import com.ssm.sdk.common.util.ErrorConstant;
import com.ssm.sdk.common.util.RequestUtil;
import com.ssm.sdk.common.util.TicketUtil;
import com.ssm.sdk.dao.UserDao;
import com.ssm.sdk.exception.SdkException;
import com.ssm.sdk.service.UserService;
import com.ssm.sdk.util.ResponseTOUtil;
import com.ssm.sdk.util.SdkConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class GameController {

    @Autowired
    private UserService userService;

    @RequestMapping("common/init.html")
    @ResponseBody
    public ResponseTO init(Integer cpid, Integer seqnum) {
        System.out.println(cpid + "-" + seqnum);
        GameTO game = GameCache.getGameByCpAndSeq(cpid, seqnum);
        Map<String, Object> result = new HashMap<>();
        result.put("content", game.getRepairContent());
        result.put("status", game.getRepairStatus());
        return ResponseTOUtil.getSuccessResult(result,null);
    }

    /**
     *  登录游戏服务器接口(要求已登录)
     * gameserverid服务器的seq_num，大区序列号。注意不是id
     * roleId 角色编号，注意i字母大写成I
     * roleName角色名，如有中文记住请求用post方式提交
     * roleGrade 角色等级
     *
     * @return
     */
    @RequestMapping("user/service/gamelogin.html")
    @ResponseBody
    public ResponseTO gamelogin(Integer cpid, Integer seqnum, Integer prtchid, Integer gameserverid, LoginServerLogTO log, String roleName, Integer roleGrade, HttpServletRequest request) throws SdkException {
        if (gameserverid == null) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (StringUtils.isEmpty(log.getIp())) {
            log.setIp(RequestUtil.getUserIpAddr(request));
        }
        GameTO game = GameCache.getGameByCpAndSeq(cpid, seqnum);
        OnlineUserTO onlineUser = (OnlineUserTO) request.getAttribute(SdkConstants.ONLINE_USER);

        log.setChannelId(prtchid);
        log.setGameId(game.getId());
        log.setUserId(onlineUser.getUserId());

        Map<String, Integer> param = new HashMap<>();
        param.put("gameId", log.getGameId());
        param.put("seqNum", gameserverid);
        Integer serverId = userService.getServerIdByGameAndSeq(param);
        if (serverId == null) {
            throw new SdkException(ErrorConstant.SERVER_NOT_EXIST);
        }
        log.setServerId(serverId);
        userService.doLoginServer(log);//更新登陆服务器日志

        // USER_DYNAMIC：(update)大区id
        UserDynamicTO userDynamicTO = new UserDynamicTO();
        userDynamicTO.setUserId(onlineUser.getUserId());
        userDynamicTO.setLastGameServerId(serverId);
        userService.updateUserDynamicServerId(userDynamicTO);

        //更新ONLINE_USER
        onlineUser.setGameServerId(serverId);
        userService.updateOnlineUser(onlineUser);

        //  根据roleId,serverId查询有没有角色，如果没有，向user_role insert数据，如果有，update数据
        UserRoleTO userRoleTO = new UserRoleTO();
        userRoleTO.setUserId(onlineUser.getUserId());
        userRoleTO.setServerId(serverId);
        userRoleTO.setRoleId(log.getRoleId());
        userRoleTO.setGameId(game.getId());
        userRoleTO.setRoleName(roleName);
        userRoleTO.setRoleLevel(roleGrade);
        if (userService.selectUserRole(userRoleTO) == null) {
            userService.addUserRole(userRoleTO);
        }else {
            userService.updateUserRole(userRoleTO);
        }

        return ResponseTOUtil.getSuccessResult(true, onlineUser.getTicket());
    }

    /**
     * 退出服务器登陆
     * 事件流 清空大区信息
     * ONLINE_USER：(update)大区ID
     */
    @RequestMapping("user/service/gamelogout.html")
    @ResponseBody
    public ResponseTO gameLogout(String ticket) throws SdkException {
        if (StringUtils.isEmpty(ticket)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        OnlineUserTO onlineUser = userService.getOnlineUser(TicketUtil.getDecodeTicket(ticket));
        if (onlineUser.getGameServerId() == null) {
            throw new SdkException(ErrorConstant.UNKOWN_ERROR);
        }
        onlineUser.setGameServerId(null);
        userService.updateOnlineUser(onlineUser);

        return ResponseTOUtil.getSuccessResult(null,null);

    }
}
