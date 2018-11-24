package com.ssm.sdk.filter;

import com.ssm.sdk.cache.CPCache;
import com.ssm.sdk.cache.ChannelCache;
import com.ssm.sdk.common.pojo.CPTO;
import com.ssm.sdk.common.pojo.ChannelTO;
import com.ssm.sdk.common.pojo.OnlineUserTO;
import com.ssm.sdk.common.util.ErrorConstant;
import com.ssm.sdk.common.util.RequestUtil;
import com.ssm.sdk.common.util.SignUtil;
import com.ssm.sdk.common.util.TicketUtil;
import com.ssm.sdk.dao.OnlineUserDao;
import com.ssm.sdk.exception.SdkException;
import com.ssm.sdk.exception.SdkExceptionHandle;
import com.ssm.sdk.util.SdkConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebFilter("*.html")
public class GlobalFilter implements Filter {
    private static final String CHECK_LOGIN_URL = "/user/service/.*";
    private static final long TWENTY_MINUTES = 60 * 20 * 1000;
    private static final long ONE_MINUTES = 60 * 1000;
    private static ApplicationContext applicationContext;
    private OnlineUserDao onlineUserDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        onlineUserDao = (OnlineUserDao) applicationContext.getBean("onlineUserDao");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        /**
         * 所有接口必须要写三个参数
         * cpid=201 厂商编号
         * seqnum=1 厂商序列号
         * prtchid=100 推广渠道号，主要用于获取国际化提示信息
         * sign根据固定算法计算出的签名（请在测试时自动加上）
         */
        try {
            String sign = RequestUtil.getString(request, "sign");
            Integer cpId = RequestUtil.getInteger(request, "cpid");
            Integer seqnum = RequestUtil.getInteger(request, "seqnum");
            Integer channelId = RequestUtil.getInteger(request, "prtchid");
            if (StringUtils.isEmpty(sign) || cpId == null || seqnum == null || channelId == null) {
                throw new SdkException(ErrorConstant.PARAM_ERROR);
            }
            CPTO cp = CPCache.getCpById(cpId);
            if (cp == null) {
                throw new SdkException(ErrorConstant.CP_NOT_EXIST);
            }
            ChannelTO channelTO = ChannelCache.getChannelById(channelId);
            if (channelTO == null) {
                throw new SdkException(ErrorConstant.CHANNEL_NOT_EXIST);
            }

            String signParam = SignUtil.getSignStr(request.getParameterMap());
            String mySign = SignUtil.getSign(signParam, cp.getSecretKey());
            System.out.println("======sign: " + sign);
            System.out.println("======SecretKey: "+cp.getSecretKey());
            System.out.println("======mySign: " + mySign);
            if (!mySign.equals(sign)) {//签名失败
                throw new SdkException(ErrorConstant.SIGN_ERROR);
            }

            String path = request.getServletPath();
            if (path.matches(CHECK_LOGIN_URL)) {
                //必须登陆
                String ticket = RequestUtil.getString(request, "ticket");
                if (StringUtils.isEmpty(ticket)) {
                    throw new SdkException(ErrorConstant.SESSION_TIME_OUT);
                }
                Date lastAct = null;
                try {
                    lastAct = TicketUtil.getTicketDate(ticket);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SdkException(ErrorConstant.PARAM_ERROR);
                }
                Date now = new Date();
                long timeMills = now.getTime() - lastAct.getTime();
                if (timeMills > TWENTY_MINUTES) {
                    throw new SdkException(ErrorConstant.SESSION_TIME_OUT);
                }
                OnlineUserTO onlineUser=onlineUserDao.getOnlineUser(TicketUtil.getDecodeTicket(ticket));
                if (timeMills > ONE_MINUTES) {
                    //更新数据库信息
                    onlineUserDao.updateOnlineUser(onlineUser);
                }
                request.setAttribute(SdkConstants.ONLINE_USER,onlineUser);
            }
        } catch (SdkException ex) {
            ex.printStackTrace();
            SdkExceptionHandle.handelException(request, response, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
