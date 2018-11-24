package com.ssm.sdk.util;

import com.ssm.sdk.cache.CPCache;
import com.ssm.sdk.common.pojo.CPTO;
import com.ssm.sdk.common.util.Base64;
import com.ssm.sdk.common.util.DESUtil;
import com.ssm.sdk.common.util.ErrorConstant;
import com.ssm.sdk.exception.SdkException;

import java.net.URLDecoder;
import java.util.Random;

public class UserUtil {
    public static boolean checkMobile(String str) {
        String reg = "^1[3456789]\\d{9}$";
        return str.matches(reg);
    }

    /**
     * 获取解密后的password，并校验
     * @param password
     * @param cpid
     * @throws SdkException
     */
    public static String getPwd(String password, Integer cpid) throws SdkException {
        try {
            CPTO cp = CPCache.getCpById(cpid);
            //DES解密
            password = DESUtil.decode(password, Base64.encode(cp.getSecretKey().getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new SdkException(ErrorConstant.UNKOWN_ERROR);
        }
        if (!checkPassword(password)) {
            throw new SdkException(ErrorConstant.INVALID_PASSWORD_INPUT);
        }
        return password;
    }

    public static boolean checkPassword(String str) {
        String reg = "^[a-zA-Z0-9]{6,20}$";
        return str.matches(reg);
    }

    public static boolean chackUserName(String str){
        String reg = "^[a-zA-Z][a-zA-Z0-9]{5,31}$";
        return str.matches(reg);
    }

    public static String getRandomUsername(){
        //todo 高并发情况待完善
        Random random=new Random();
        String time=System.currentTimeMillis()+"";
        StringBuilder builder=new StringBuilder("l");
        builder.append(time.substring(7));
        builder.append(random.nextInt(10));
        return builder.toString();
    }
}
