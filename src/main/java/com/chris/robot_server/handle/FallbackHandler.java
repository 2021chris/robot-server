package com.chris.robot_server.handle;

import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.model.Update;

/**
 * 处理未被其他Handler支持的事件
 */
@Component
public class FallbackHandler implements BaseHandler {

    @Override
    public boolean supports(Update update) {
        return true;
    }

    @Override
    public void handle(Update update) {
        System.out.println("Unhandled update: " + update.updateId());
    }

    @Override
    public int priority() {
        return 999;
    }
}