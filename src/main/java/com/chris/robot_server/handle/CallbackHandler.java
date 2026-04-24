package com.chris.robot_server.handle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.chris.robot_server.config.sysConfig;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.TelegramTextUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.RequiredArgsConstructor;

/**
 * 处理回调查询（如按钮点击）
 */
@Component
@RequiredArgsConstructor
public class CallbackHandler implements BaseHandler {

    private final TelegramLinkMapper telegramLinkMapper;
    private final TelegramGroupMapper telegramGroupMapper;
    private final PushService pushService;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    

    @Override
    public boolean supports(Update update) {
        return update.callbackQuery() != null;
    }

    @Override
    public void handle(TelegramBot bot, String token, Update update) {
        CallbackQuery query = update.callbackQuery();
        Long chatId = resolveChatId(query);
        if (chatId == null)
            return;

        String data = query.data();
        //==============开奖历史
        if ("xin-aomen".equals(data)) {
            pushService.handleLotteryImage(bot,chatId);
        } else if ("hongkong".equals(data)) {
            pushService.handleLotteryXgImage(bot,chatId);
        }else if("kl8".equals(data)) {
            pushService.handleLotteryKlImage(bot,chatId);
        }
        // =============挑码助手
         else if ("tm".equals(data)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        else if ("tiaoma".equals(data)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        //============设置快乐8_香港_新澳门_澳门|新澳实时开奖
        else if("open-kl8".equals(data)) {

            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }

            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean kl8Enabled = isEnabledFront(telegramGroup.getSettings(), 0);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 0, !kl8Enabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-hongkong".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean xgEnabled = isEnabledFront(telegramGroup.getSettings(), 1);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 1, !xgEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-xin-aomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean xinaomenEnabled = isEnabledFront(telegramGroup.getSettings(), 2);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 2, !xinaomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-aomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean aomenEnabled = isEnabledFront(telegramGroup.getSettings(), 3);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 3, !aomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-shishi-xinaomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean shishiXinaomenEnabled = isEnabledBack(telegramGroup.getSettings());
            String updatedSettings = updateSettingBack(telegramGroup.getSettings(), !shishiXinaomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
    }

    private void sendDaoHangMenu(String settings, TelegramBot bot, Long chatId) {
        if(settings == null) {
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
                        new InlineKeyboardButton(shishiXinaomen == 1 ? "✅新澳六合彩实时开奖" : "❌新澳六合彩实时开奖").callbackData("open-shishi-xinaomen"),
                });
        String messIdStr = redisTemplate.opsForValue().get(sysConfig.BOT_GROUP_DAHHANG_KEY + chatId);
        if(messIdStr != null) {
            bot.execute(new EditMessageText(chatId,Integer.parseInt(messIdStr), "选择开奖订阅功能：").replyMarkup(keyboard));
        }
        
    }

    private String updateSettingsFront(String settings, int index, boolean enable) {
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        if (index < frontParts.length) {
            frontParts[index] = enable ? "1" : "0";
            parts[0] = String.join("_", frontParts);
            settings = parts[0] + "|" + parts[1];
        }
        return settings;
    }

    private String updateSettingBack(String settings, boolean enable) {
        String[] parts = settings.split("\\|");
        if (parts.length == 2) {
            parts[1] = enable ? "1" : "0";
            settings = parts[0] + "|" + parts[1];
        }
        return settings;
    }

    private boolean isEnabledFront(String settings, int index) {
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        if (index < frontParts.length) {
            return "1".equals(frontParts[index]);
        }
        return false;
    }

    private boolean isEnabledBack(String settings) {
        String[] parts = settings.split("\\|");
        if (parts.length == 2) {
            return "1".equals(parts[1]);
        }
        return false;
    }

    // 唯一允许 deprecated API 的地方（隔离）
    private Long resolveChatId(CallbackQuery query) {
        try {
            if (query.message() != null && query.message().chat() != null) {
                return query.message().chat().id();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public int priority() {
        return 5;
    }



    private String getCaptionTelegramLinks(String type) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        // sb.append("🎯 <b> 挑码助手</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📌 <b>")
                    .append(TelegramTextUtil.escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(TelegramTextUtil.escape(link.getAddress()))
                    .append("\">")
                    .append(TelegramTextUtil.escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }

}
