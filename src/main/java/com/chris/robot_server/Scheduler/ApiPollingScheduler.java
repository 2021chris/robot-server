package com.chris.robot_server.Scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.chris.robot_server.Worker.BaseLotteryWorker;
import com.chris.robot_server.Worker.Kl8Worker;
import com.chris.robot_server.Worker.XianggangWorker;
import com.chris.robot_server.Worker.XinaoWorker;


@Component
public class ApiPollingScheduler {

    private ScheduledExecutorService executor;

    private final XinaoWorker xinaoWorker;
    private final XianggangWorker xianggangWorker;
    private final Kl8Worker kl8Worker;


    public ApiPollingScheduler(XinaoWorker xinaoWorker, XianggangWorker xianggangWorker, Kl8Worker kl8Worker) {
        this.xinaoWorker = xinaoWorker;
        this.xianggangWorker = xianggangWorker;
        this.kl8Worker = kl8Worker;
    }

    // 每天 21:30:00 启动任务（北京时间）
    // @Scheduled(cron = "0 30 21 * * ?", zone = "Asia/Shanghai")
    @Scheduled(cron = "0 30 21 * * ?", zone = "Asia/Shanghai")
    public synchronized void startPolling() {
        if (executor != null && !executor.isShutdown()) {
            return; // 防止重复启动
        }

        executor = Executors.newScheduledThreadPool(3);
        scheduleNext(executor, xinaoWorker);
        scheduleNext(executor, xianggangWorker);
        scheduleNext(executor, kl8Worker);

        System.out.println("✅ Polling started at " + LocalDateTime.now());
    }

    // 每天 21:36:00 停止任务
    @Scheduled(cron = "0 36 21 * * ?", zone = "Asia/Shanghai")
    public synchronized void stopPolling() {
        if (executor != null) {
            executor.shutdown();
            System.out.println("🛑 Polling stopped at " + LocalDateTime.now());
        }
    }

    // 每轮 4–8 秒随机调度
    private void scheduleNext(ScheduledExecutorService executor, BaseLotteryWorker<?> worker) {
        if (executor == null || executor.isShutdown()) return;
        // worker.run();// 测试
        int delay = ThreadLocalRandom.current().nextInt(4, 9);
        executor.schedule(() -> {
            try {
                worker.run();
            } finally {
                scheduleNext(executor, worker);
            }
        }, delay, TimeUnit.SECONDS);
    }
    
}
