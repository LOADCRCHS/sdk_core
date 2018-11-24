package com.ssm.sdk.dao;

import com.ssm.sdk.common.pojo.GameTO;

import java.util.List;
import java.util.Map;

public interface GameDao {
    List<GameTO> getAllGameList();

    Integer getServerIdByGameAndSeq(Map param);
}
