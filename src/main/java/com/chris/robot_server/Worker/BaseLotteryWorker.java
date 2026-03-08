package com.chris.robot_server.Worker;

import org.springframework.beans.factory.annotation.Autowired;

import com.chris.robot_server.component.WorkerRateLimiter;

public abstract class BaseLotteryWorker<T> {

    @Autowired
    WorkerRateLimiter limiter;

    public final void run() {
        limiter.acquire();
        try {
            fetchAndProcess();
        } catch (Exception ignored) {}
    }

    protected abstract void fetchAndProcess();
}
