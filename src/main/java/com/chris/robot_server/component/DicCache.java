package com.chris.robot_server.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * DicCache
 * 项目启动的时候，加载数据到缓存:模拟账户等
 * @author chris
 * 11/8/24
 */
@Component
@Slf4j
public class DicCache {


    @Bean
    public void init(){
        log.info("系统启动中。。。加载模拟账户数据到缓存");
        
        log.info("数据加载完成");
    }


}
