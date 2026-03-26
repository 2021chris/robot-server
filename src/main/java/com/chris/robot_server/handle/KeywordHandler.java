package com.chris.robot_server.handle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.TelegramLink;
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

/**
 * 处理关键词回复
 */
@Component
public class KeywordHandler implements BaseHandler {

    private final TelegramBot bot;
    private final TelegramLinkMapper telegramLinkMapper;
    private final ImageGeneratorService imageGeneratorService;
    private final LotteryHistoryMapper lotteryHistoryMapper;

    public KeywordHandler(TelegramBot bot, TelegramLinkMapper telegramLinkMapper,
            ImageGeneratorService imageGeneratorService, LotteryHistoryMapper lotteryHistoryMapper) {
        this.bot = bot;
        this.telegramLinkMapper = telegramLinkMapper;
        this.imageGeneratorService = imageGeneratorService;
        this.lotteryHistoryMapper = lotteryHistoryMapper;
    }

    @Override
    public boolean supports(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    @Override
    public void handle(Update update) {
        String text = TelegramTextUtil.normalize(update.message().text());
        long chatId = update.message().chat().id();

        if ("历史".equals(text) || "开奖".equals(text) || "k".equals(text)) {
            handleLotteryImage(chatId);
        }
        if ("挑码".equals(text) || "挑码助手".equals(text) || "t".equals(text) || "挑".equals(text)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        if ("设置彩种".equals(text)) {
            sendSetting(chatId);
        }
        if ("生肖".equals(text) || "波色".equals(text) || "s".equals(text)) {
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/s3.jpg").parseMode(ParseMode.HTML));
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/s4.jpg").parseMode(ParseMode.HTML));
        }
        if ("复式".equals(text) || "组合".equals(text) || "f".equals(text)) {
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/fushi.jpg").parseMode(ParseMode.HTML));
        }
        if ("导航".equals(text) || "d".equals(text)) {
            sendMenu(chatId);
        }

    }

    private void sendMenu(long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("彩票注册投注网").callbackData("cpzctzw")
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("TG官方下载网址").callbackData("tggfxzwz"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("代开会员直登号").callbackData("dkhyzdh"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("民间USDT承兑员").callbackData("mjucdy"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("安全使用中文包").callbackData("aqsyzwb"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("极速赛车计划群").callbackData("jsscpqq"),
                });

        bot.execute(new SendMessage(chatId, "导航：").replyMarkup(keyboard));
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

    private void handleLotteryImage(Long chatId) {
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
            String newFileId = sendAndGetFileId(chatId, imageBytes, "新澳门六合彩");
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
    private String sendAndGetFileId(Long chatId, byte[] imageBytes, String title) {

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

    private void sendSetting(long chatId) {
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