package com.ssm.sdk.cache;

import com.ssm.sdk.common.cache.AbstractCache;
import com.ssm.sdk.common.pojo.CPTO;
import com.ssm.sdk.service.CPService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CPCache extends AbstractCache {
    private Logger logger = Logger.getLogger(CPCache.class);
    private static Map<Integer, CPTO> cpMap = new HashMap<>();

    @Autowired
    private CPService cpService;

    @Override
    public void update() {
        logger.info("start update CP=====");
        List<CPTO> cps = cpService.getAllCPList();
        Map<Integer, CPTO> temp = new HashMap<>();
        for (CPTO cp : cps) {
            temp.put(cp.getId(), cp);
        }
        if (temp != null && !temp.isEmpty()) {
            cpMap = temp;
        }
        logger.info("complete update CP=====");
    }

    public static CPTO getCpById(Integer id) {
        return cpMap.get(id);
    }
}
