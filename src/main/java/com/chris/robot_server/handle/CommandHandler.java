package com.chris.robot_server.handle;

import org.springframework.stereotype.Component;

import com.chris.robot_server.util.TelegramTextUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.RequiredArgsConstructor;

/**
 * 处理命令消息（如 /menu、/help）
 */
@Component
@RequiredArgsConstructor
public class CommandHandler implements BaseHandler {


        @Override
        public boolean supports(Update update) {
                return update.message() != null && update.message().text() != null
                                && update.message().text().startsWith("/");
        }

        @Override
        public void handle(TelegramBot bot, String token, Update update) {
                Message msg = update.message();
                long chatId = msg.chat().id();

                String text = TelegramTextUtil.normalize(msg.text());
                String cmd = TelegramTextUtil.extractCommand(text);

                switch (cmd) {
                        case "/menu":
                                sendMenu(bot, chatId);
                                break;
                        default:
                                bot.execute(new SendMessage(chatId, "未知命令"));
                }
        }


        private void sendMenu(TelegramBot bot, long chatId) {
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                                new InlineKeyboardButton[] {
                                                new InlineKeyboardButton("挑码助手").callbackData("tiaoma")
                                },
                                new InlineKeyboardButton[] {
                                                new InlineKeyboardButton("新澳六合彩开奖历史").callbackData("xin-aomen"),
                                },
                                new InlineKeyboardButton[] {
                                                new InlineKeyboardButton("香港六合彩开奖历史").callbackData("hongkong"),
                                },
                                new InlineKeyboardButton[] {
                                                new InlineKeyboardButton("快乐8六合彩开奖历史").callbackData("kl8"),
                                });

                bot.execute(new SendMessage(chatId, "菜单：").replyMarkup(keyboard));
        }

        @Override
        public int priority() {
                return 10;
        }
}