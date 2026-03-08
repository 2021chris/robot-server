package com.chris.robot_server.component;

import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.RateLimiter;

@Component
public class WorkerRateLimiter {

    private final RateLimiter limiter = RateLimiter.create(0.5); // 每秒最多 0.5 次

    public void acquire() {
        limiter.acquire();
    }
}