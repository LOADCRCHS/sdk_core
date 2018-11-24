package com.ssm.sdk.util;


import com.ssm.sdk.common.pojo.ResponseTO;
import com.ssm.sdk.common.util.TicketUtil;

public class ResponseTOUtil {

    public static ResponseTO getSuccessResult(Object object, String ticket) {

        ResponseTO resp = new ResponseTO();
        resp.setSuccess(true);
        resp.setResult(object);
        if (ticket != null) {
            resp.setTicket(TicketUtil.getEncodeTicket(ticket));
        }
        return resp;
    }

    public static ResponseTO getErrorResult(Object object,String ticket) {
        ResponseTO resp = new ResponseTO();
        resp.setSuccess(false);
        resp.setResult(object);
        resp.setTicket(ticket);
        return resp;
    }
}
