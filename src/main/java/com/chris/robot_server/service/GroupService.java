package com.chris.robot_server.service;

import java.util.List;

import com.pengrad.telegrambot.model.Chat;

public interface GroupService {

    void saveGroup(Chat chat);

    List<Long> getAllGroupIds();

    int count();
}
