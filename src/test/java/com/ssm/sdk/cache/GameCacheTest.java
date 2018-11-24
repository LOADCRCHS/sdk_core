package com.ssm.sdk.cache; 

import com.ssm.sdk.common.pojo.GameTO;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/** 
* GameCache Tester. 
* 
* @author <Authors name> 
* @since <pre>Ê®ÔÂ 29, 2018</pre> 
* @version 1.0 
*/ 
public class GameCacheTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: update() 
* 
*/ 
@Test
public void testUpdate() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getGameByCpAndSeq(Integer cpId, Integer seqNum) 
* 
*/ 
@Test
public void testGetGameByCpAndSeq() throws Exception {
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
    GameCache gameCache = (GameCache) applicationContext.getBean("gameCache");
    gameCache.start();
    gameCache.update();
    GameTO gameByCpAndSeq = GameCache.getGameByCpAndSeq(201, 1);
    System.out.println(gameByCpAndSeq);

} 


} 
