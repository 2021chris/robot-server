package com.chris.robot_server.handle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.config.sysConfig;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.enums.KeywordTypeEnum;
import com.chris.robot_server.model.BotKeyword;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.service.BotKeywordService;
import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryRow;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import lombok.RequiredArgsConstructor;

/**
 * 处理关键词回复
 */
@Component
@RequiredArgsConstructor
public class KeywordHandler implements BaseHandler {

    private final TelegramLinkMapper telegramLinkMapper;
    private final ImageGeneratorService imageGeneratorService;
    private final LotteryHistoryMapper lotteryHistoryMapper;
    private final BotKeywordService botKeywordService;
    private final TelegramGroupMapper telegramGroupMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean supports(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    @Override
    public void handle(TelegramBot bot, String token, Update update) {
        String text = TelegramTextUtil.normalize(update.message().text());
        long chatId = update.message().chat().id();// 群id

        if ("历史".equals(text) || "开奖".equals(text) || "k".equals(text)) {
            handleLotteryImage(bot, chatId);
        }
        if ("挑码".equals(text) || "挑码助手".equals(text) || "t".equals(text) || "挑".equals(text)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        if ("设置".equals(text)) {
            Long userId = update.message().from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            sendSetting(bot, chatId, token);
        }
        // 非菜单关键词
        List<BotKeyword> keywords = botKeywordService.getAllEnabledKeywords();

        for (BotKeyword kw : keywords) {
            if (kw.getKeyWord() != null && kw.getKeyWord().toLowerCase().equals(text)) {
                if (kw.getType() == KeywordTypeEnum.Html.getCode()) {
                    bot.execute(new SendMessage(chatId, kw.getKeyValue()).parseMode(ParseMode.HTML));
                } else if (kw.getType() == KeywordTypeEnum.WordLink.getCode()) {
                    bot.execute(new SendMessage(chatId, kw.getKeyValue()));
                }
                return;// 匹配到第一个就回复
            }

        }
    }

    

    private String getCaptionTelegramLinks(String type) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 <b> 挑码助手</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📌 <b>")
                    .append(escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(escape(link.getAddress()))
                    .append("\">")
                    .append(escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }

    private String escape(String text) {
        return HtmlUtils.htmlEscape(text);
    }

    private void handleLotteryImage(TelegramBot bot, Long chatId) {
        List<LotteryHistory> list = lotteryHistoryMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistory first = list.get(0);
        // String fileId = first.getFileId();
        // if (fileId != null) {
        // sendByFileId(chatId, fileId, "新澳门六合彩");
        // return;
        // } else {
        List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                .collect(Collectors.toList());
        try {
            byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
            // 3️⃣ 上传图片 → 获取 file_id
            String newFileId = sendAndGetFileId(bot, chatId, imageBytes, "新澳门六合彩");
            if (newFileId != null) {
                // 4️⃣ 保存 file_id 到数据库
                first.setFileId(newFileId);
                lotteryHistoryMapper.updateByPrimaryKeySelective(first);
            }
        } catch (IOException e) {
            bot.execute(new SendMessage(chatId, "数据获取失败"));
            e.printStackTrace();
        }
        // }
    }

    /**
     * 发送图片字节数组到指定聊天ID并获取文件ID
     *
     * @param chatId     聊天ID
     * @param imageBytes 图片字节数组
     * @return 图片文件ID（如果成功），否则为null
     */
    private String sendAndGetFileId(TelegramBot bot, Long chatId, byte[] imageBytes, String title) {

        String caption = getCaptionTelegramLinks("kj", title);

        SendPhoto req = new SendPhoto(chatId, imageBytes)
                .caption(caption)
                .parseMode(ParseMode.HTML);

        SendResponse res = bot.execute(req);

        if (!res.isOk()) {
            System.err.println("Send image failed: " + res.errorCode());
            return null;
        }

        Message msg = res.message();

        if (msg.photo() != null && msg.photo().length > 0) {
            return msg.photo()[msg.photo().length - 1].fileId(); // 最高质量图
        }

        return null;
    }

    private String getCaptionTelegramLinks(String type, String title) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 <b> ").append(escape(title)).append(" 开奖记录</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📢 <b>")
                    .append(escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(escape(link.getAddress()))
                    .append("\">")
                    .append(escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }

    private void sendSetting(TelegramBot bot, long chatId, String token) {
        TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
        System.out.println("当前群设置: " + telegramGroup);
        if (telegramGroup == null) {
            bot.execute(new SendMessage(chatId, "需要在群/频道中设置"));
            return;
        }
        // TODO 只能管理员设置
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
        return 20;
    }
}