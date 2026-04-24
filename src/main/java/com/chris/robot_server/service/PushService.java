package com.chris.robot_server.service;

import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.pengrad.telegrambot.TelegramBot;

public interface PushService {

    public void pushToGroups(LotteryHistoryVO vo,TelegramGroup group);

    public void handleLotteryImage(TelegramBot bot,Long chatId);

    public void handleLotteryKlImage(TelegramBot bot, Long chatId);

    public void handleLotteryXgImage(TelegramBot bot,Long chatId);

    public void handleLotterylaoImage(TelegramBot bot,Long chatId);

}
