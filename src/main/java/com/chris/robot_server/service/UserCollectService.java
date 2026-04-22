package com.chris.robot_server.service;

import com.pengrad.telegrambot.model.Update;

public interface UserCollectService {

    /**
     * 采集用户信息（推荐在收到 message 或 newChatMembers 时调用）
     */
    public void collectUser(Update update, long botId, String token);
}
