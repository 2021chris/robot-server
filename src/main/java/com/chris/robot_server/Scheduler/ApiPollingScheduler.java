package com.chris.robot_server.Scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.chris.robot_server.Worker.BaseLotteryWorker;
import com.chris.robot_server.Worker.Kl8Worker;
import com.chris.robot_server.Worker.LaoaoWorker;
import com.chris.robot_server.Worker.XianggangWorker;
import com.chris.robot_server.Worker.XinaoWorker;
import com.chris.robot_server.service.DrawService;


@Component
public class ApiPollingScheduler {

    @Autowired
    private DrawService drawService;

    private ScheduledExecutorService executor;

    private final XinaoWorker xinaoWorker;
    private final XianggangWorker xianggangWorker;
    private final Kl8Worker kl8Worker;
    private final LaoaoWorker laoaoWorker;


    public ApiPollingScheduler(XinaoWorker xinaoWorker, XianggangWorker xianggangWorker, Kl8Worker kl8Worker, LaoaoWorker laoaoWorker) {
        this.xinaoWorker = xinaoWorker;
        this.xianggangWorker = xianggangWorker;
        this.kl8Worker = kl8Worker;
        this.laoaoWorker = laoaoWorker;
    }

    // 每天 21:30:00 启动任务（北京时间）
    // @Scheduled(cron = "0 30 21 * * ?", zone = "Asia/Shanghai")
    @Scheduled(cron = "0 30 21 * * ?", zone = "Asia/Shanghai")
    public synchronized void startPolling() {
        if (executor != null && !executor.isShutdown()) {
            return; // 防止重复启动
        }

        executor = Executors.newScheduledThreadPool(4);
        scheduleNext(executor, xinaoWorker);
        scheduleNext(executor, xianggangWorker);
        scheduleNext(executor, kl8Worker);
        scheduleNext(executor, laoaoWorker);

        System.out.println("✅ Polling started at " + LocalDateTime.now());
    }

    // 每天 21:36:30 停止任务
    @Scheduled(cron = "30 36 21 * * ?", zone = "Asia/Shanghai")
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


    // 21:36:15 执行一次
    @Scheduled(cron = "35 36 21 * * ?", zone = "Asia/Shanghai")
    public void runOnceAfterStop() {
        // 在这里写你要执行的逻辑
        // 新开奖全体推送
        drawService.pushAllLotteryDraw();
        
    }
    
}
