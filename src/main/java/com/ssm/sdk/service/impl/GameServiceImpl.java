package com.ssm.sdk.service.impl;

import com.ssm.sdk.common.pojo.GameTO;
import com.ssm.sdk.dao.GameDao;
import com.ssm.sdk.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private GameDao gameDAO;

    @Override
    public List<GameTO> getAllGameList() {
        return gameDAO.getAllGameList();
    }
}
