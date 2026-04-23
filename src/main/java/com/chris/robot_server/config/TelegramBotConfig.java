package com.chris.robot_server.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chris.robot_server.dao.TelegramBotsMapper;
import com.chris.robot_server.model.TelegramBots;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

@Configuration
public class TelegramBotConfig {

    private final TelegramBotsMapper telegramBotsMapper;
    private final String baseUrl;

    public TelegramBotConfig(TelegramBotsMapper telegramBotsMapper,
            @Value("${telegram.bot-base-url}") String baseUrl) {
        this.telegramBotsMapper = telegramBotsMapper;
        this.baseUrl = baseUrl;
    }

    /**
     * 存放所有活跃的 TelegramBot 实例（key = token）
     */
    @Bean
    public Map<String, TelegramBot> botMap() {
        Map<String, TelegramBot> botMap = new ConcurrentHashMap<>();

        List<TelegramBots> enabledBots = telegramBotsMapper.selectAllBots();

        for (TelegramBots entity : enabledBots) {
            TelegramBot bot = new TelegramBot(entity.getToken());
            botMap.put(entity.getToken(), bot);

            // @TODO启动时重新设置 Webhook（安全可靠）
            setWebhook(bot, entity.getToken());
        }

        return botMap;
    }

    @Bean
    public Map<String, Long> botIdMap() {
        Map<String, Long> botIdMap = new ConcurrentHashMap<>();
        
        // 从数据库加载所有机器人
        List<TelegramBots> enabledBots = telegramBotsMapper.selectAllBots();
        
        enabledBots.forEach(bot -> botIdMap.put(bot.getToken(), bot.getBotId()));
        return botIdMap;
    }

    private void setWebhook(TelegramBot bot, String token) {
        String webhookUrl = baseUrl + "/webhook/" + token;

        SetWebhook setWebhook = new SetWebhook()
                .url(webhookUrl)
                .allowedUpdates("message", "callback_query", "inline_query", "my_chat_member", "channel_post")
                .maxConnections(100)
                .dropPendingUpdates(true); // 推荐：启动时清空旧的待处理消息

        // 执行并获取响应
        BaseResponse response = bot.execute(setWebhook);

        if (response.isOk()) {
            System.out.println("✅ Webhook 设置成功 → " + webhookUrl);
        } else {
            System.err.println("❌ Webhook 设置失败: " + response.description());
        }
    }
}
