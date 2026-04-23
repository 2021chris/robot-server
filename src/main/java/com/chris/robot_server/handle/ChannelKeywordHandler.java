package com.chris.robot_server.handle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.chris.robot_server.config.sysConfig;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.util.TelegramTextUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import lombok.RequiredArgsConstructor;

/**
 * 处理频道消息的关键词回复
 */
@Component
@RequiredArgsConstructor
public class ChannelKeywordHandler implements BaseHandler {

    private final TelegramGroupMapper telegramGroupMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean supports(Update update) {
        return update.channelPost() != null && update.channelPost().text() != null;
    }

    @Override
    public void handle(TelegramBot bot, String token, Update update) {
        String text = TelegramTextUtil.normalize(update.channelPost().text());
        long chatId = update.channelPost().chat().id();
        // 处理频道消息的关键词回复逻辑
        if ("设置".equals(text)) {
            sendSetting(bot, chatId, token);
        }
    }

    private void sendSetting(TelegramBot bot, long chatId, String token) {
        TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
        System.out.println("当前频道设置: " + telegramGroup);
        if (telegramGroup == null) {
            bot.execute(new SendMessage(chatId, "需要在群/频道中设置"));
            return;
        }
        String settings = telegramGroup.getSettings();
        if (settings == null) {
            settings = "1_0_0_0|0";// 快乐8_香港_新澳门_澳门|新澳实时开奖
        }
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        int shishiXinaomen = Integer.parseInt(parts[1]);
        int kl8 = Integer.parseInt(frontParts[0]);
        int hongkong = Integer.parseInt(frontParts[1]);
        int xinAomen = Integer.parseInt(frontParts[2]);
        int aomen = Integer.parseInt(frontParts[3]);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(kl8 == 1 ? "✅快乐8六合彩" : "❌快乐8六合彩").callbackData("open-kl8"),
                        new InlineKeyboardButton(hongkong == 1 ? "✅香港六合彩" : "❌香港六合彩").callbackData("open-hongkong"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(xinAomen == 1 ? "✅新澳门六合彩" : "❌新澳门六合彩").callbackData("open-xin-aomen"),
                        new InlineKeyboardButton(aomen == 1 ? "✅澳门六合彩" : "❌澳门六合彩").callbackData("open-aomen"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(shishiXinaomen == 1 ? "✅新澳六合彩实时开奖" : "❌新澳六合彩实时开奖")
                                .callbackData("open-shishi-xinaomen"),
                });

        SendResponse resp = bot.execute(new SendMessage(chatId, "选择开奖订阅功能：").replyMarkup(keyboard));
        if (resp.isOk()) {
            String botGroupKey = sysConfig.BOT_GROUP_DAHHANG_KEY + chatId;
            redisTemplate.opsForValue().set(botGroupKey, resp.message().messageId().toString());
        }

    }

    @Override
    public int priority() {
        return 25;
    }

}
