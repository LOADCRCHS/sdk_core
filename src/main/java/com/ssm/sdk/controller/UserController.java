package com.ssm.sdk.controller;

import com.ssm.sdk.cache.CPCache;
import com.ssm.sdk.common.pojo.*;
import com.ssm.sdk.common.util.*;
import com.ssm.sdk.exception.SdkException;
import com.ssm.sdk.service.UserService;
import com.ssm.sdk.util.ResponseTOUtil;
import com.ssm.sdk.util.SdkConstants;
import com.ssm.sdk.util.SendMsgUtil;
import com.ssm.sdk.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 绑定手机获取验证码接口
     *
     * @param account 用户名
     * @param num     手机号
     */
    @RequestMapping("user/bind/sendcode.html")
    @ResponseBody
    public ResponseTO sendBindCode(String account, String num) throws SdkException {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(num)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (UserUtil.checkMobile(num)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (UserUtil.chackUserName(account)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (userService.getUserIdByName(account) == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        if (userService.getUserIdByNum(account) != null) {
            throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
        }
        boolean success = userService.doSendVerCode(num);
        return ResponseTOUtil.getSuccessResult(success, null);
    }

    /*忘记密码发送短信接口*/
    @RequestMapping("user/findpwd/sendcode.html")
    @ResponseBody
    public ResponseTO sendFindPwdCode(String account) throws SdkException {
        if (StringUtils.isEmpty(account) || !UserUtil.checkMobile(account)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (userService.getUserIdByNum(account) == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }

        boolean success = userService.doSendVerCode(account);
        return ResponseTOUtil.getSuccessResult(success, null);
    }

    /**
     * 注册发送短信接口
     *
     * @param account 手机号
     */
    @RequestMapping("user/reg/sendcode.html")
    @ResponseBody
    public ResponseTO sendRegisteCode(String account) throws SdkException {
        if (StringUtils.isEmpty(account) || !UserUtil.checkMobile(account)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (userService.getUserIdByNum(account) != null) {
            throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
        }
        boolean success = userService.doSendVerCode(account);
        return ResponseTOUtil.getSuccessResult(success, null);
    }

    /**
     * 用户注册接口
     * type=1普通注册
     * type =0&uid==null游客注册
     * type =0&uid=123游客绑定账号
     *
     * @param account    用户名或手机号
     * @param password   密码,需要传DES加密后的密码
     * @param repassword 确认密码，和密码相同
     * @param type       用户类型，1代表普通用户，0是游客。普通注册必须是1
     * @param code       验证码
     * @param uid        已经登录的游客的用户id
     * @param prtchid    推广渠道号，主要用于获取国际化提示信息
     * @param cpid       厂商编号
     */
    @RequestMapping("user/register.html")
    @ResponseBody
    public ResponseTO regist(HttpServletRequest request, HttpServletResponse response, String account, String password, String repassword, Integer type, String code, Integer uid, Integer prtchid, Integer cpid) throws SdkException {
        if (type == null || (type != 0 && type != 1)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        ResponseTO responseTO = null;
        if (type == 0) {
            if (StringUtils.isEmpty(account)) {
                //游客注册
                account = UserUtil.getRandomUsername();
                password = SendMsgUtil.getRandomCode(6);
                UserTO user = userService.doRegister(account, password, type, prtchid);
                user.setPassword(password);
                responseTO = ResponseTOUtil.getSuccessResult(user, null);


                // 注册成功后自动登陆
                try {
                    CPTO cp = CPCache.getCpById(cpid);
                    password = URLEncoder.encode(DESUtil.encode(password, Base64.encode(cp.getSecretKey().getBytes())), "utf-8");
                    request.getRequestDispatcher("login.html?account=" + account + "&password=" + password).forward(request, response);
                    return responseTO;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SdkException(ErrorConstant.UNKOWN_ERROR);
                }

            } else {
                //游客绑定账号
                if (StringUtils.isEmpty(password) || StringUtils.isEmpty(repassword) || uid == null) {
                    throw new SdkException(ErrorConstant.PARAM_ERROR);
                }
                password = checkUser(account, password, repassword, code, cpid);
                UserTO user = userService.doBindAccount(account, password, uid);
                user.setPassword(password);
                responseTO = ResponseTOUtil.getSuccessResult(user, null);
            }
        } else {
            //普通用户注册
            if (StringUtils.isEmpty(account) || StringUtils.isEmpty(password) || StringUtils.isEmpty(repassword)) {
                throw new SdkException(ErrorConstant.PARAM_ERROR);
            }
            password = checkUser(account, password, repassword, code, cpid);
            UserTO user = userService.doRegister(account, password, type, prtchid);
            user.setPassword(password);
            responseTO = ResponseTOUtil.getSuccessResult(user, null);
        }

        // 注册成功后自动登陆
        try {
            request.getRequestDispatcher("login.html").forward(request, response);
            return responseTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SdkException(ErrorConstant.UNKOWN_ERROR);
        }
    }

    /**
     * 登陆接口
     *
     * @param account  用户名或手机号
     * @param password 密码
     * @param prtchid  渠道id
     * @param cpid     厂商id
     * @param seqnum   游戏序号
     * @param ip       登陆ip
     * @param ua       机型
     */
    @RequestMapping("user/login.html")
    @ResponseBody
    public ResponseTO login(String account, String password, Integer prtchid, Integer cpid, Integer seqnum, String ip, String ua, HttpServletRequest request) throws SdkException {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(account)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (StringUtils.isEmpty(ip)) {
            ip = RequestUtil.getUserIpAddr(request);
        }
        return userService.doLogin(account, password, prtchid, cpid, seqnum, ip, ua);
    }

    /**
     * 重置密码接口
     *
     * @param account  手机号
     * @param password 新密码 DES加密
     * @param code     验证码
     */
    @RequestMapping("user/findpwd/reset.html")
    @ResponseBody
    public ResponseTO reset(String account, String password, String code, Integer cpid, HttpServletResponse response, HttpServletRequest request) throws SdkException {
        if (StringUtils.isEmpty(account) || !UserUtil.checkMobile(account)) {
            throw new SdkException(ErrorConstant.INVALID_MOBILE_INPUT);
        }
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(password)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        if (userService.getUserIdByNum(account) == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        password = UserUtil.getPwd(password, cpid);
        ResponseTO responseTO = userService.updateUserPwd(account, password, code);

        try {
            //验证成功后自动登陆
            request.getRequestDispatcher("../login.html").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SdkException(ErrorConstant.UNKOWN_ERROR);
        }
        return responseTO;
    }

    /**
     * 心跳接口
     * 在客户端每隔6分钟调用一次，更新用户在线状态
     * @param ticket 登陆票据
     */
    @RequestMapping("user/service/heartbeat.html")
    @ResponseBody
    public ResponseTO heartbeat(String ticket) {
        String decodeTicket = TicketUtil.getDecodeTicket(ticket);
        String newTicket = TicketUtil.getEncodeTicket(decodeTicket);
        return ResponseTOUtil.getSuccessResult(null, newTicket);
    }

    /*10.绑定手机*/
    @RequestMapping("user/bindnum.html")
    @ResponseBody
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
        if (userService.getUserIdByName(account) == null) {
            throw new SdkException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        if (userService.getUserIdByNum(num) != null) {
            throw new SdkException(ErrorConstant.PHONE_ALREADY_EXIST);
        }
        return userService.bindNum(account, num, code);
    }

    private static final long TWENTY_MINUTES = 60 * 20 * 1000;

    /**
     * 退出登陆
     */
    @RequestMapping("user/service/logout.html")
    @ResponseBody
    public ResponseTO gameLogOut(String ticket,HttpServletRequest request) throws SdkException {
        //todo
        if (StringUtils.isEmpty(ticket)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }

        //更新数据库信息
        userService.updateLogoutDate(TicketUtil.getDecodeTicket(ticket));
        request.setAttribute(SdkConstants.ONLINE_USER,null);

        return ResponseTOUtil.getSuccessResult(null,null);
    }

    /**
     * 检测用户是否在线
     */
    @RequestMapping("user/service/checkonlineuser.html")
    @ResponseBody
    public ResponseTO checkOnlineUser(String ticket, Integer userId) throws SdkException {
        if (StringUtils.isEmpty(ticket) || StringUtils.isEmpty(userId)) {
            throw new SdkException(ErrorConstant.PARAM_ERROR);
        }
        OnlineUserTO onlineUser = userService.getOnlineUser(TicketUtil.getDecodeTicket(ticket));
        if (onlineUser == null) {
            throw new SdkException(ErrorConstant.NEED_LOGIN);
        }
        if (!onlineUser.getUserId().equals(userId)) {
            throw new SdkException(ErrorConstant.NEED_LOGIN);
        }
        return ResponseTOUtil.getSuccessResult(null, null);
    }

    //todo checkUser 重复代码
    private String checkUser(String account, String password, String repassword, String code, Integer cpid) throws SdkException {
        if (!password.equals(repassword)) {
            throw new SdkException(ErrorConstant.PASSWORD_REPASSWORD_UNEQUAL);
        }
        CPTO cp = CPCache.getCpById(cpid);

        try {
            //DES解密
            password = DESUtil.decode(password, Base64.encode(cp.getSecretKey().getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new SdkException(ErrorConstant.INVALID_PASSWORD_INPUT);
        }
        if (!UserUtil.checkPassword(password)) {
            throw new SdkException(ErrorConstant.INVALID_PASSWORD_INPUT);
        }
        if (UserUtil.checkMobile(account)) {
            //手机号
            if (StringUtils.isEmpty(code)) {
                throw new SdkException(ErrorConstant.PARAM_ERROR);
            }
            VerCodeTO verCode = userService.getCodeByMobile(account);
            if (verCode == null || !verCode.getCode().equals(code)) {
                throw new SdkException(ErrorConstant.PARAM_ERROR);
            }
            userService.deleteCodeByMobile(account);//验证通过后，删除验证码，验证码失效
        } else if (UserUtil.chackUserName(account)) {
            //用户名

        } else {//非法输入
            throw new SdkException(ErrorConstant.INVALID_ACCOUNT_INPUT);
        }
        return password;
    }
}
