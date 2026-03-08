package com.chris.robot_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Chris
 * @date 9/21/21 11:49 AM
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {

    /**
     * 自定义线程池配置
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(
                100, 300, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(Integer.MAX_VALUE),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
