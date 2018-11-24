package com.ssm.sdk.exception;

import com.ssm.sdk.cache.ChannelCache;
import com.ssm.sdk.cache.LocalPromptCache;
import com.ssm.sdk.common.pojo.ChannelTO;
import com.ssm.sdk.common.pojo.ErrorCodeTO;
import com.ssm.sdk.common.pojo.LocalPromptTO;
import com.ssm.sdk.common.util.ErrorConstant;
import com.ssm.sdk.common.util.JsonUtil;
import com.ssm.sdk.common.util.RequestUtil;
import com.ssm.sdk.util.ResponseTOUtil;
import com.ssm.sdk.util.SdkConstants;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SdkExceptionHandle implements HandlerExceptionResolver {

    public static void handelException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        ErrorCodeTO errorCodeTO = new ErrorCodeTO();
        LocalPromptTO local = null;

        try {
            Integer channelId = RequestUtil.getInteger(request, "prtchid");
            ChannelTO channelTO = ChannelCache.getChannelById(channelId);

            if(ex instanceof SdkException){
                SdkException e = (SdkException) ex;
                if (channelTO == null) {
                    local = LocalPromptCache.getLocalPromptByLanguageAndName(SdkConstants.DEFAULT_LANGUAGE, e.getMessage());
                }else {
                    local = LocalPromptCache.getLocalPromptByLanguageAndName(channelTO.getLanguage(), e.getMessage());
                }
            }else {
                local = LocalPromptCache.getLocalPromptByLanguageAndName(SdkConstants.DEFAULT_LANGUAGE, ErrorConstant.UNKOWN_ERROR);
            }
        } catch (Exception e) {
            local = LocalPromptCache.getLocalPromptByLanguageAndName(SdkConstants.DEFAULT_LANGUAGE, ErrorConstant.UNKOWN_ERROR);
        }
        errorCodeTO.setCode(local.getCode());
        errorCodeTO.setMsg(local.getName());
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(JsonUtil.getJSON(ResponseTOUtil.getErrorResult(errorCodeTO,null)));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        handelException(request, response, e);
        return null;
    }
}
