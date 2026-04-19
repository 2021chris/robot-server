package com.chris.robot_server.handle;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

import lombok.RequiredArgsConstructor;

/**
 * 分发Update到不同的Handler处理
 * message 普通消息
 * callbackQuery 按钮点击
 * inlineQuery inline 模式
 * editedMessage 编辑消息
 * join/leave 成员变动
 */
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {
    private final List<BaseHandler> handlers;


    // 你原来的业务服务、命令处理器等全部注入这里
    public void handle(TelegramBot bot, String token, Update update) {
        // 按 order 排序（优先级高的先执行）
        handlers.stream()
                .sorted(Comparator.comparingInt(BaseHandler::priority))
                .filter(h -> h.supports(update))
                .findFirst()                     // 只执行第一个匹配的（责任链）
                .ifPresent(h -> h.handle(bot, token, update));
    }

}
