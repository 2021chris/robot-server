package com.chris.robot_server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.GroupService;
import com.pengrad.telegrambot.model.Chat;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private TelegramGroupMapper telegramGroupMapper;

    public void saveGroup(Chat chat) {
        TelegramGroup group = new TelegramGroup();
        group.setGroupId(chat.id());
        group.setTitle(chat.title());
        group.setType(chat.type() != null ? chat.type().name() : null);

        telegramGroupMapper.insert(group);
    }

    public List<Long> getAllGroupIds() {
        return telegramGroupMapper.findAll().stream()
            .map(TelegramGroup::getGroupId)
            .toList();
    }

    public int count() {
        return telegramGroupMapper.count();
    }
}
