package com.chris.robot_server.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chris.robot_server.dao.TelegramBotsMapper;
import com.chris.robot_server.model.TelegramBots;
import com.chris.robot_server.service.BotManagementService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetMeResponse;

@Service
public class BotManagementServiceImpl implements BotManagementService {

    private final TelegramBotsMapper telegramBotsMapper;
    private final Map<String, TelegramBot> botMap;
    private final String baseUrl; // 同上

    public BotManagementServiceImpl(TelegramBotsMapper telegramBotsMapper,
            Map<String, TelegramBot> botMap,
            @Value("${telegram.bot-base-url}") String baseUrl) {
        this.telegramBotsMapper = telegramBotsMapper;
        this.botMap = botMap;
        this.baseUrl = baseUrl;
    }

    @Override
    @Transactional
    public TelegramBots addBot(String token, String description) {

        TelegramBots botEntity = telegramBotsMapper.findByToken(token);
        if (botEntity != null) {
            return null;
        }
        // 1. 验证 token 是否有效
        TelegramBot tempBot = new TelegramBot(token);
        BaseResponse meResponse = tempBot.execute(new GetMe()); // GetMe 返回的也是 BaseResponse 包装

        if (!meResponse.isOk()) {
            // throw new RuntimeException("Token 无效: " + meResponse.description());
            return null;
        }
        // 获取用户信息
        User botUser = ((GetMeResponse) meResponse).user(); // 如果需要获取 bot 信息，可强转

        TelegramBots newBot = new TelegramBots();
        newBot.setToken(token);
        newBot.setDescription(description);
        newBot.setBotname(botUser.username() != null ? botUser.username() : "unknown");
        newBot.setBotId(botUser.id());
        newBot.setStatus((byte)1);
        newBot.setGroupNumber(0);
        telegramBotsMapper.insert(newBot); // 插入数据库，生成 ID

        TelegramBot bot = new TelegramBot(token);
        botMap.put(token, bot);

        setWebhook(bot, token);

        System.out.println("🚀 新机器人添加成功: @" + newBot.getBotname());
        return newBot;
    }

    /**
     * 统一的 Webhook 设置方法
     */
    private void setWebhook(TelegramBot bot, String token) {
        String webhookUrl = baseUrl + "/webhook/" + token;

        SetWebhook setWebhook = new SetWebhook()
                .url(webhookUrl)
                .allowedUpdates("message", "callback_query", "inline_query", "my_chat_member", "channel_post")
                .maxConnections(100)
                .dropPendingUpdates(true);

        BaseResponse response = bot.execute(setWebhook);

        if (response.isOk()) {
            System.out.println("✅ Webhook 设置成功 → " + webhookUrl);
        } else {
            System.err.println("❌ Webhook 设置失败: " + response.description());
        }
    }

    /**
     * 重要：切换环境（本地 ngrok → 线上）时调用此方法
     * 只更新 Webhook，不插入数据库记录
     */
    @Override
    public void resetWebhook(String token) {
        TelegramBots botEntity = telegramBotsMapper.findByToken(token);
        if (botEntity == null) {
            throw new RuntimeException("该 token 不存在于数据库中");
        }

        TelegramBot bot = botMap.get(token);
        if (bot == null) {
            bot = new TelegramBot(token);
            botMap.put(token, bot);
        }

        setWebhook(bot, token);

    }

    /**
     * 禁用机器人：更新数据库状态、删除 Webhook、从内存中移除实例
     * 
     * @param id
     */
    @Override
    public TelegramBots disableBot(Long id) {
        TelegramBots bots = telegramBotsMapper.selectByPrimaryKey(id);
        if (bots == null) {
            throw new RuntimeException("Bot 不存在");
        }
        bots.setStatus((byte) 0); // 设置为禁用
        telegramBotsMapper.updateByPrimaryKeySelective(bots); // 更新数据库
        TelegramBot removed = botMap.remove(bots.getToken());
        if (removed != null) {
            removed.execute(new DeleteWebhook());
        }
        System.out.println("🛑 机器人已停用: " + bots.getBotname());
        return bots;
    }

    /**
     * 启用/恢复机器人（最关键的方法）
     */
    @Override
    public TelegramBots enableBot(Long id) {
        TelegramBots bots = telegramBotsMapper.selectByPrimaryKey(id);
        if (bots == null) {
            throw new RuntimeException("Bot 不存在");
        }

        if (bots.getStatus() == 1) {
            throw new RuntimeException("该机器人已经是启用状态");
        }

        // 1. 更新数据库状态为启用
        bots.setStatus((byte) 1);
        telegramBotsMapper.updateByPrimaryKeySelective(bots);

        // 2. 创建 TelegramBot 实例并放入内存 Map
        TelegramBot bot = new TelegramBot(bots.getToken());
        botMap.put(bots.getToken(), bot);

        // 3. 重新设置 Webhook（最重要！）
        setWebhook(bot, bots.getToken());

        System.out.println("✅ 机器人已恢复启用: " + bots.getBotname()
                + " | Webhook 已重新设置");

        return bots;
    }

    @Override
    public List<TelegramBots> listAll() {
        return telegramBotsMapper.selectAllBots();
    }

}
