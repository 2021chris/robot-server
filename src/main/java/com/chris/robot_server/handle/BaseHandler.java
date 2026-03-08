package com.chris.robot_server.handle;

import com.pengrad.telegrambot.model.Update;

public interface BaseHandler {
    int priority(); // 优先级
    boolean supports(Update update);
    void handle(Update update);
}
