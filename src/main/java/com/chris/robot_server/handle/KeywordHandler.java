package com.chris.robot_server.handle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.enums.KeywordTypeEnum;
import com.chris.robot_server.model.BotKeyword;
import com.chris.robot_server.model.LotteryHistory;
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


    @Override
    public boolean supports(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    @Override
    public void handle(TelegramBot bot, String token, Update update) {
        String text = TelegramTextUtil.normalize(update.message().text());
        long chatId = update.message().chat().id();

        if ("历史".equals(text) || "开奖".equals(text) || "k".equals(text)) {
            handleLotteryImage(bot, chatId);
        }
        if ("挑码".equals(text) || "挑码助手".equals(text) || "t".equals(text) || "挑".equals(text)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        if ("设置彩种".equals(text)) {
            sendSetting(bot, chatId);
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

    private void sendSetting(TelegramBot bot, long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("新澳六合彩").callbackData("open-xin-aomen"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("香港六合彩").callbackData("open-hongkong"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("快乐8六合彩").callbackData("open-kl8"),
                });

        bot.execute(new SendMessage(chatId, "请选择每日定时推送彩种：").replyMarkup(keyboard));
    }

    @Override
    public int priority() {
        return 20;
    }
}