package com.ssm.sdk.util;

import com.ssm.sdk.common.pojo.VerCodeTO;
import com.ssm.sdk.common.util.HttpClientUtil;
import com.ssm.sdk.common.util.JsonUtil;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

public class SendMsgUtil {

//    private static Logger logger = Logger.getLogger(SendMsgUtil.class);

    public static String getRandomCode(Integer len) {
        if (len == null) {
            return "";
        }
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    public static boolean send(VerCodeTO codeTO) {
        /*http://v.juhe.cn/sms/send?mobile=手机号码&tpl_id=短信模板ID&tpl_value=%23code%23%3D654654&key=*/
        if (codeTO == null) {
            return false;
        }
        String url = "http://v.juhe.cn/sms/send";
        String code = URLEncoder.encode("#code#=" + codeTO.getCode());
        String key = "8034f7150df6d200c4b0c3934cb4d900";
        String tpl_id = "110322";
        String param = "mobile=" + codeTO.getMobile() + "&tpl_id=" + tpl_id + "&tpl_value=" + code + "&key="+key;
//        String tpl_id = "110130";
//        String key = "babd9ff9a559a3f84adfb6eaf51c5aa1";

        String result = HttpClientUtil.doPost(url, param);
        HashMap<String, Object> map = (HashMap) JsonUtil.getObj(result, HashMap.class);

        if(map.get("error_code")==null){
//            logger.info("短信发送失败========");
            return false;
        }

        return ((int) map.get("error_code"))==0;
    }
}
