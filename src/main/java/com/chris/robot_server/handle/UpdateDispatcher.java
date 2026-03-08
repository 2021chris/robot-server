package com.chris.robot_server.handle;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.model.Update;

/**
 * 分发Update到不同的Handler处理
 * message 普通消息
 * callbackQuery 按钮点击
 * inlineQuery inline 模式
 * editedMessage 编辑消息
 * join/leave 成员变动
 */
@Component
public class UpdateDispatcher {
    private final List<BaseHandler> handlers;

    public UpdateDispatcher(List<BaseHandler> handlers) {
        this.handlers = handlers.stream()
                .sorted(Comparator.comparingInt(BaseHandler::priority).reversed())
                .toList();
    }

    public void dispatch(Update update) {
        for (BaseHandler h : handlers.stream()
                .sorted(Comparator.comparingInt(BaseHandler::priority))
                .toList()) {

            if (h.supports(update)) {
                System.out.println("Dispatching to: " + h.getClass().getSimpleName());
                h.handle(update);
                return;
            }
        }

        System.out.println("Unhandled update: " + update.updateId());
    }

}
