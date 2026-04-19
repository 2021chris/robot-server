package com.chris.robot_server.service;

import java.util.List;

import com.chris.robot_server.model.TelegramBots;

public interface BotManagementService {

    public TelegramBots addBot(String token, String description);

    public void resetWebhook(String token);

    /**
     * 禁用机器人（实际是更新状态）
     * @param id
     */
    public TelegramBots disableBot(Long id);

    public TelegramBots enableBot(Long id);

    public List<TelegramBots> listAll();
}
