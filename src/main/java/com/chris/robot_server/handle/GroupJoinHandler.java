package com.chris.robot_server.handle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.model.TelegramGroup;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.ChatMember;

/**
 * 处理群聊加入事件
 */
@Component
public class GroupJoinHandler implements BaseHandler {

    @Autowired
    private TelegramGroupMapper telegramGroupMapper;


    @Override
    public void handle(Update u) {

        System.out.println("GroupJoinHandler HANDLE triggered");

        var m = u.myChatMember();
        var chat = m.chat();

        var oldStatus = m.oldChatMember().status();
        var newStatus = m.newChatMember().status();

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

        if (joined) {
            System.out.println("JOIN DETECTED: " + title);
            saveGroup(groupId, title, type);
        }

        if (left) {
            System.out.println("BOT REMOVED: " + title);
            deleteGroup(groupId);
        }
    }

    private void saveGroup(Long groupId, String title, String type) {
        if (!telegramGroupMapper.existsByGroupId(groupId)) {
            telegramGroupMapper.insert(new TelegramGroup(groupId, title, type));
            System.out.println("DB INSERT OK: " + groupId);
        } else {
            TelegramGroup uGroup = new TelegramGroup();
            uGroup.setGroupId(groupId);
            uGroup.setTitle(title);
            telegramGroupMapper.updateByPrimaryKeySelective(uGroup);
            System.out.println("DB UPDATE OK: " + groupId);
        }
    }

    private void deleteGroup(Long groupId) {
        telegramGroupMapper.deleteByGroupId(groupId);
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