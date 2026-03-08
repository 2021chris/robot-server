package com.chris.robot_server.handle;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.util.TelegramTextUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

/**
 * 处理关键词回复
 */
@Component
public class KeywordHandler implements BaseHandler {

    private final TelegramBot bot;
    private final TelegramLinkMapper telegramLinkMapper;


    public KeywordHandler(TelegramBot bot, TelegramLinkMapper telegramLinkMapper) {
        this.bot = bot;
        this.telegramLinkMapper = telegramLinkMapper;
    }

    @Override
    public boolean supports(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    @Override
    public void handle(Update update) {
        String text = TelegramTextUtil.normalize(update.message().text());
        long chatId = update.message().chat().id();

        if (text.contains("历史")||text.contains("开奖")) {
            sendMenu(chatId);
        }
        if(text.contains("挑码")) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        if (text.contains("设置")) {
            sendSetting(chatId);
        }
        if (text.contains("生肖")) {
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/s3.jpg").parseMode(ParseMode.HTML));
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/s4.jpg").parseMode(ParseMode.HTML));
        }
        if (text.contains("复式")) {
            bot.execute(new SendMessage(chatId, "https://img.xn--6-yq0c.com/fushi.jpg").parseMode(ParseMode.HTML));
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

    private void sendMenu(long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("新澳六合彩开奖历史").callbackData("xin-aomen"),
                },new InlineKeyboardButton[] {
                        new InlineKeyboardButton("香港六合彩开奖历史").callbackData("hongkong"),
                },new InlineKeyboardButton[] {
                        new InlineKeyboardButton("快乐8六合彩开奖历史").callbackData("kl8"),
                });

        bot.execute(new SendMessage(chatId, "请选择彩种：").replyMarkup(keyboard));
    }

    private void sendSetting(long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton("新澳六合彩").callbackData("open-xin-aomen"),
                },new InlineKeyboardButton[] {
                        new InlineKeyboardButton("香港六合彩").callbackData("open-hongkong"),
                },new InlineKeyboardButton[] {
                        new InlineKeyboardButton("快乐8六合彩").callbackData("open-kl8"),
                });

        bot.execute(new SendMessage(chatId, "请选择每日定时推送彩种：").replyMarkup(keyboard));

        bot.execute(new SendMessage(keyboard, "彩满天邀请码【661146】🙊\r\n" + //
                        "万利下载地址1：168wl.app\r\n" + //
                        "万利下载地址2：https://wanli.hzlqsg.com/\r\n" + //
                        "畅玩网页版1: wanli68.com \r\n" + //
                        "畅玩网页版2: wanli98.cc"));
    }

    @Override
    public int priority() {
        return 20;
    }
}