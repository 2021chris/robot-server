package com.chris.robot_server.handle;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.model.TelegramGroup;
import com.pengrad.telegrambot.model.Update;

import lombok.RequiredArgsConstructor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMember.Status;
import com.pengrad.telegrambot.model.ChatMemberUpdated;

/**
 * 处理群聊加入事件
 */
@Component
@RequiredArgsConstructor
public class GroupJoinHandler implements BaseHandler {

    private final TelegramGroupMapper telegramGroupMapper;


    @Override
    public void handle(TelegramBot bot, String token, Update update) {

        System.out.println("GroupJoinHandler HANDLE triggered");

        ChatMemberUpdated myChat = update.myChatMember();
        Chat chat = myChat.chat();

        Status oldStatus = myChat.oldChatMember().status();
        Status newStatus = myChat.newChatMember().status();

        System.out.println("BOT STATUS CHANGE: " + oldStatus + " -> " + newStatus);

        boolean joined =
                (oldStatus == ChatMember.Status.left || oldStatus == ChatMember.Status.kicked)
             && (newStatus == ChatMember.Status.member || newStatus == ChatMember.Status.administrator);

        boolean left =
                (oldStatus == ChatMember.Status.member || oldStatus == ChatMember.Status.administrator)
             && (newStatus == ChatMember.Status.left || newStatus == ChatMember.Status.kicked);

        Long groupId = chat.id();
        String title = chat.title();
        String type = chat.type().toString();

        Long currentBotId = myChat.newChatMember().user().id();

        if (joined) {
            System.out.println("JOIN DETECTED: " + title);
            saveGroup(groupId, title, type, currentBotId, token);
        }

        if (left) {
            System.out.println("BOT REMOVED: " + title);
            deleteGroup(groupId, token);
        }
    }

    private void saveGroup(Long groupId, String title, String type, Long botId, String token) {
        if (!telegramGroupMapper.existsByGroupIdAndToken(groupId, token)) {
            telegramGroupMapper.insert(new TelegramGroup(groupId, title, type, botId, token, "1_0_0_0|0"));
            System.out.println("DB INSERT OK: " + groupId);
        } else {
            TelegramGroup uGroup = new TelegramGroup();
            uGroup.setGroupId(groupId);
            uGroup.setTitle(title);
            uGroup.setToken(token);
            telegramGroupMapper.updateByPrimaryKeySelective(uGroup);
            System.out.println("DB UPDATE OK: " + groupId);
        }
    }

    private void deleteGroup(Long groupId, String token) {
        telegramGroupMapper.deleteByGroupIdAndToken(groupId, token);
        System.out.println("DB DELETE OK: " + groupId);
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public boolean supports(Update u) {
        boolean ok = u != null && u.myChatMember() != null;

        if (ok) {
            System.out.println("GroupJoinHandler supports: TRUE");
        }

        return ok;
    }

}