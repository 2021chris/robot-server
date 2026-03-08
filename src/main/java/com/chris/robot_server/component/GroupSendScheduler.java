package com.chris.robot_server.component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.chris.robot_server.model.TelegramGroup;

@Component
public class GroupSendScheduler {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void dispatch(List<TelegramGroup> groups, Consumer<TelegramGroup> task) {
        for (TelegramGroup group : groups) {
            executor.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(600, 1600));
                    task.accept(group);
                } catch (Exception ignored) {}
            });
        }
    }
}
