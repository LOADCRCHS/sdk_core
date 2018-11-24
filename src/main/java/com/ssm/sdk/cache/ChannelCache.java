package com.ssm.sdk.cache;

import com.ssm.sdk.common.cache.AbstractCache;
import com.ssm.sdk.common.pojo.ChannelTO;
import com.ssm.sdk.service.ChannelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChannelCache extends AbstractCache {

    private Logger logger = Logger.getLogger(ChannelCache.class);

    private static Map<Integer, ChannelTO> channelMap = new HashMap<>();

    @Autowired
    private ChannelService channelService;

    @Override
    public void update() {
        logger.info("start update ChannelTO=====");

        List<ChannelTO> channels = channelService.getAllChannels();
        Map<Integer, ChannelTO> temp = new HashMap<>();
        for (ChannelTO ch : channels) {
            temp.put(ch.getId(), ch);
        }
        if (temp != null && !temp.isEmpty()) {
            channelMap = temp;
        }

        logger.info("complete update ChannelTO=====");
    }

    public static ChannelTO getChannelById(Integer id) {
        return channelMap.get(id);
    }
}
