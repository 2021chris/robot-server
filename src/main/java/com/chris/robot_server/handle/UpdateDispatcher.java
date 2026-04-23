package com.chris.robot_server.handle;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.chris.robot_server.service.UserCollectService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetMeResponse;

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
    private final UserCollectService userCollectService;
    private final Map<String, Long> botIdMap; // token → botId



    // 你原来的业务服务、命令处理器等全部注入这里
    public void handle(TelegramBot bot, String token, Update update) {
        // 采集用户
        if (shouldCollectUser(update)) {
            Long botId = botIdMap.get(token); // 从映射中获取 botId
            userCollectService.collectUser(update,botId, token);   // token 作为 botId
        }

        // 按 order 排序（优先级高的先执行）
        handlers.stream()
                .sorted(Comparator.comparingInt(BaseHandler::priority))
                .filter(h -> h.supports(update))
                .findFirst()                     // 只执行第一个匹配的（责任链）
                .ifPresent(h -> h.handle(bot, token, update));
    }

    /**
     * 判断是否需要采集用户信息（过滤掉不必要的更新）
     */
    private boolean shouldCollectUser(Update update) {
        return update.message() != null 
                || (update.message() != null && update.message().newChatMembers() != null)
                || update.myChatMember() != null;
    }

}
