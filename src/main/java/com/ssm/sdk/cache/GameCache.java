package com.ssm.sdk.cache;

import com.ssm.sdk.common.cache.AbstractCache;
import com.ssm.sdk.common.pojo.GameTO;
import com.ssm.sdk.service.GameService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GameCache extends AbstractCache {

    private Logger logger = Logger.getLogger(GameCache.class);

    @Autowired
    private GameService gameService;

    private static Map<String, GameTO> gameMap = new HashMap<>();

    @Override
    public void update() {
        List<GameTO> gameList = gameService.getAllGameList();
        Map<String, GameTO> tempMap = new HashMap<>();
        logger.info("start update AllGameList ==========");
        for (GameTO gameTO : gameList) {
            tempMap.put(gameTO.getCpId() + "-" + gameTO.getSeqNum(), gameTO);
        }
        if (tempMap != null && !tempMap.isEmpty()) {
            gameMap = tempMap;
        }
        logger.info("complete update AllGameList ==========");
    }

    public static GameTO getGameByCpAndSeq(Integer cpId, Integer seqNum) {
        if (cpId == null || seqNum == null) {
            return null;
        }
        return gameMap.get(cpId + "-" + seqNum);
    }
}
