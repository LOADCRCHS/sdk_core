package com.ssm.sdk.cache;

import com.ssm.sdk.common.cache.AbstractCache;
import com.ssm.sdk.common.pojo.LocalPromptTO;
import com.ssm.sdk.service.LocalPromptService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LocalPromptCache extends AbstractCache {
    private Logger logger = Logger.getLogger(LocalPromptCache.class);
    @Autowired
    private LocalPromptService localPromptService;

    private static Map<String, LocalPromptTO> localMap = new HashMap<>();


    @Override
    public void update() {
        logger.info("start update LocalPrompt=====");

        List<LocalPromptTO> locals = localPromptService.getAllPrompt();
        Map<String, LocalPromptTO> tempMap = new HashMap<>();
        for (LocalPromptTO temp : locals) {
            tempMap.put(temp.getLanguageType() + "-" + temp.getName(), temp);
        }
        if (tempMap != null && !tempMap.isEmpty()) {
            localMap = tempMap;
        }
        logger.info("complete update LocalPrompt=====");
    }

    public static LocalPromptTO getLocalPromptByLanguageAndName(Integer languageType, String name) {
        if (languageType == null || name == null) {
            return null;
        }
        return localMap.get(languageType + "-" + name);
    }
}
