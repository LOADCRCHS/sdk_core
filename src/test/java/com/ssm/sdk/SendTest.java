package com.ssm.sdk;

import com.ssm.sdk.common.util.Base64;
import com.ssm.sdk.common.util.DESUtil;
import com.ssm.sdk.common.util.HttpClientUtil;
import com.ssm.sdk.common.util.SignUtil;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class SendTest {

    @Test
    public void checkUserOnline() throws UnsupportedEncodingException {
        String url = "user/service/checkonlineuser.html";
        Map<String, String> param = new HashMap<>();
        param.put("ticket", "1a16a890952198b97665aac9831cc4c083acb368b8478");
        param.put("userId", "87289");
        send(url, param);
    }

    @Test
    public void logOutTest() throws UnsupportedEncodingException {
        String url = "user/service/logout.html";
        Map<String, String> param = new HashMap<>();
        param.put("ticket", "d70b576fcdb767468ecd5543d2d8eee518fd54af6d8e7");
        send(url, param);
    }

    @Test
    public void logOutServerTest() throws UnsupportedEncodingException {
        String url = "user/service/gamelogout.html";
        Map<String, String> param = new HashMap<>();
        param.put("ticket", "d70b576fcdb767468ecd5543d2d8eee518fd54af6d8e7");
        send(url, param);
    }

    @Test
    public void loginServerTest() throws UnsupportedEncodingException {
        /* gameserverid服务器的seq_num，大区序列号。注意不是id
         * roleId 角色编号，注意i字母大写成I
         * roleName角色名，如有中文记住请求用post方式提交
         * roleGrade 角色等级*/
        String url = "user/service/gamelogin.html";
        Map<String, String> param = new HashMap<>();
        param.put("gameserverid", "9999");
        param.put("ticket", "d70b576fcdb767468ecd5543d2d8eee518fd54af6d8e7");
        param.put("roleId", "0002");
        param.put("roleName", "yr");
        param.put("roleGrade", "1");
        send(url, param);
    }

    @Test
    public void bindNumTest() throws Exception {
        String url = "user/bindnum.html";
        Map<String, String> param = new HashMap<>();
        param.put("account", "a18975261877");
        param.put("num", "18975261877");
        param.put("code", "0438");
        send(url, param);
    }

    @Test
    public void resetTest() throws Exception {
        //修改密码
        String url = "user/findpwd/reset.html";
        Map<String, String> param = new HashMap<>();
        param.put("password", DESUtil.encode("276597", Base64.encode("8cP7Vbru".getBytes())));
        param.put("account", "18975261877");
        param.put("code", "0438");
        send(url, param);
    }

    @Test
    public void heartBeat() throws Exception {
        String url = "user/service/heartbeat.html";
        Map<String, String> param = new HashMap<>();
        param.put("ticket", "d70b576fcdb767468ecd5543d2d8eee518fd54af6d8e7");

        send(url, param);

    }

    @Test
    public void loginTest() throws Exception {
        String url = "user/login.html";
        Map<String, String> param = new HashMap<>();
        param.put("password", DESUtil.encode("456789", Base64.encode("8cP7Vbru".getBytes())));
        param.put("account", "13931603951");
        //d70b576fcdb767468ecd5543d2d8eee518fd54af6d8e7
        send(url, param);
    }


    @Test
    public void registerTest() throws Exception {

//        String url = "user/reg/sendcode.html";
        String url = "user/register.html";
        Map<String, String> param = new HashMap<>();
        param.put("password", DESUtil.encode("456789", Base64.encode("8cP7Vbru".getBytes())));
        param.put("repassword", DESUtil.encode("456789", Base64.encode("8cP7Vbru".getBytes())));
        param.put("type", "1");
//        param.put("uid", "87279");
        param.put("account", "13931603951");
        param.put("code", "2306");
        send(url, param);

    }

    @Test
    public void sendCodeTest() throws UnsupportedEncodingException {
        String url = "user/reg/sendcode.html";
//        String url = "user/findpwd/sendcode.html";
        Map<String, String> param = new HashMap<>();
        param.put("account", "13931603951");
        send(url, param);
    }


    public void send(String url, Map<String, String> param) throws UnsupportedEncodingException {
        param.put("cpid", "201");
        param.put("seqnum", "1");
        param.put("prtchid", "100");
        String sign = SignUtil.getSign(getSignStr(param), "8cP7Vbru");
        String result = doPost("http://localhost:8080/" + url, getSignStr(param) + "&sign=" + sign);
        System.out.println("http://localhost:8080/" + url + "?" + getSignStr(param) + "&sign=" + sign);
        System.out.println(result);
    }

    public static String getSignStr(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder builder = new StringBuilder();
        for (String paramName : keys) {
            System.out.print(paramName + "-");
            if ("sign".equals(paramName)) {
                continue;
            }
            String value = params.get(paramName);
            if (value != null) {
                builder.append("&");
                builder.append(paramName).append("=");
                builder.append(URLEncoder.encode(value, "utf-8"));
            } else {
                builder.append("&");
                builder.append(paramName).append("=");
            }
            System.out.println(value);
        }
        return builder.substring(1).toString();
    }

    public String doPost(String httpUrl, String param) {
        return HttpClientUtil.doPost(httpUrl, param);
    }
}
