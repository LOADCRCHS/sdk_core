package com.ssm.sdk.service.impl; 

import com.ssm.sdk.common.pojo.GameTO;
import com.ssm.sdk.dao.GameDao;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/** 
* GameServiceImpl Tester. 
* 
* @author <Authors name> 
* @since <pre>Ê®ÔÂ 29, 2018</pre> 
* @version 1.0 
*/ 
public class GameServiceImplTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getAllGameList() 
* 
*/ 
@Test
public void testGetAllGameList() throws Exception {
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
    GameDao gameDao = (GameDao) applicationContext.getBean("gameDao");
    List<GameTO> allGameList = gameDao.getAllGameList();
    for (GameTO gameTO : allGameList) {
        System.out.println(gameTO);
    }
}

} 
