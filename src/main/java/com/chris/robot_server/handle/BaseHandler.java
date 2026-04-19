package com.chris.robot_server.handle;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

public interface BaseHandler {
    int priority(); // 优先级
    boolean supports(Update update);
    /**
     * 执行具体处理逻辑
     * @param bot     当前机器人实例
     * @param token   bot token（如果需要区分机器人）
     * @param update  收到的更新
     */
    void handle(TelegramBot bot, String token, Update update);
}
