package com.chris.robot_server.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.LotteryMessageBuilder;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final Map<String, TelegramBot> botMap; // token → TelegramBot

    @Override
    public void send(long groupId, String text, String token) {
        try {
            TelegramBot bot = botMap.get(token);
            if (bot != null) {
                bot.execute(new SendMessage(groupId, text));
            }
            Thread.sleep(1200);
        } catch (Exception ignored) {}
    }

    private final Map<Long, Integer> lastMessageMap = new ConcurrentHashMap<>();


    public void pushToGroups(LotteryHistoryVO vo,TelegramGroup group) {
        String text = LotteryMessageBuilder.build(vo);
        Integer msgId = lastMessageMap.get(group.getGroupId());
        String token = group.getToken();
        if (msgId == null) {
            TelegramBot bot = botMap.get(token);
            if (bot != null) {
                var resp = bot.execute(new SendMessage(group.getGroupId(), text));
                if (resp.isOk()) {
                    lastMessageMap.put(group.getGroupId(), resp.message().messageId());
                }
            }
        } else {
            TelegramBot bot = botMap.get(token);
            if (bot != null) {
                bot.execute(new EditMessageText(group.getGroupId(), msgId, text));
            }
            if(vo.getNumbers().size()>=7) {
                lastMessageMap.remove(group.getGroupId());
            }
        }
    }

}
