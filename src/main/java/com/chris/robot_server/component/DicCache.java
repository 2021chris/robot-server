package com.chris.robot_server.component;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.chris.robot_server.config.StaticConfig;
import com.chris.robot_server.dao.TelegramUserMapper;
import com.chris.robot_server.model.TelegramUser;


/**
 * DicCache
 * 项目启动的时候，加载数据到缓存:模拟账户等
 * @author chris
 * 11/8/24
 */
// @Component
@Slf4j
public class DicCache {

    @Autowired
    private TelegramUserMapper telegramUserMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    
    @Bean
    public void init(){
        log.info("系统启动中。。。加载模拟账户数据到缓存");
        log.info("数据加载完成");
    }



}
