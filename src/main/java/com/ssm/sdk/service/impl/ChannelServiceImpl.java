package com.ssm.sdk.service.impl;

import com.ssm.sdk.common.pojo.ChannelTO;
import com.ssm.sdk.dao.ChannelDao;
import com.ssm.sdk.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {

    @Autowired
    private ChannelDao channelDAO;

    @Override
    public List<ChannelTO> getAllChannels() {
        return channelDAO.getAllChannels();
    }
}
